<script>
  import { onMount, onDestroy } from 'svelte';

  export let trackingEndpoint = '/api/v1/analytics/search-events';
  export let searchApiEndpoint = '/api/v1/materials/search';
  export let nowFn = () => performance.now();
  export let fetchFn = null;

  let searchQuery = '';
  let activeSearchQuery = '';
  let selectedCategory = '';
  let selectedTag = '';
  let sortBy = 'relevance';
  let sortOrder = 'desc';

  // Sample dataset for fallback / initial display
  const sampleDocuments = [
    {
      id: '123e4567-e89b-12d3-a456-426614174000',
      title: 'Standard Protocol for Influenza Outbreak Investigation',
      summary: 'Comprehensive guidelines and standardized protocols for field investigation of suspected influenza outbreaks and cluster control.',
      category: 'protocol',
      tags: ['influenza', 'outbreak', 'surveillance'],
      author: 'Epidemiology Research Group',
      createdAt: '2026-08-01T10:00:00Z',
      updatedAt: '2026-08-15T14:30:00Z',
      fileUrl: '/files/protocols/influenza-investigation-2026.pdf'
    },
    {
      id: '223e4567-e89b-12d3-a456-426614174001',
      title: 'Q3 Epidemiological Surveillance Report 2023',
      summary: 'Quarterly statistical overview of respiratory viral pathogen transmission rates across urban centers.',
      category: 'report',
      tags: ['respiratory', 'surveillance', 'statistics'],
      author: 'WHO Global Observatory',
      createdAt: '2023-10-15T08:00:00Z',
      updatedAt: '2023-10-20T12:00:00Z',
      fileUrl: '/files/reports/q3-surveillance-2023.pdf'
    },
    {
      id: '323e4567-e89b-12d3-a456-426614174002',
      title: 'Global Influenza A (H1N1) Surveillance Data 2010-2020',
      summary: 'Comprehensive dataset aggregating ten years of global surveillance records, detailing strain variations and geographic distribution.',
      category: 'dataset',
      tags: ['influenza', 'h1n1', 'dataset'],
      author: 'Global Health Data Institute',
      createdAt: '2024-01-10T09:00:00Z',
      updatedAt: '2024-01-11T10:00:00Z',
      fileUrl: '/files/datasets/h1n1-surveillance-2010-2020.csv'
    },
    {
      id: '423e4567-e89b-12d3-a456-426614174003',
      title: 'Ebola Response Guidelines v2.1',
      summary: 'Updated clinical trial and field response guidelines for managing zoonotic spillover containment in emergency settings.',
      category: 'guideline',
      tags: ['ebola', 'outbreak', 'guideline'],
      author: 'Nexus BioLabs Research',
      createdAt: '2023-08-05T14:00:00Z',
      updatedAt: '2023-08-06T16:00:00Z',
      fileUrl: '/files/guidelines/ebola-response-v2.1.pdf'
    },
    {
      id: '523e4567-e89b-12d3-a456-426614174004',
      title: 'SARS-CoV-2 Variant Transmission in Urban Centers',
      summary: 'Extensive longitudinal study mapping rapid spread and evolutionary divergence of viral strains across metropolitan areas.',
      category: 'report',
      tags: ['covid', 'transmission', 'urban'],
      author: 'Dr. Elena Rostova, et al.',
      createdAt: '2023-10-01T11:00:00Z',
      updatedAt: '2023-10-02T13:00:00Z',
      fileUrl: '/files/reports/sars-cov-2-transmission.pdf'
    }
  ];

  let searchResults = [...sampleDocuments];
  let totalElements = sampleDocuments.length;
  let isLoading = false;
  let searchError = null;
  let downloadError = null;
  let failedDownloadDoc = null;

  let searchCompleted = false;
  let searchCompletedAt = null;
  let hasClickedDocument = false;
  let isAbandoned = false;
  let trackedEvents = [];

  function getEffectiveFetch() {
    if (fetchFn) return fetchFn;
    return null;
  }

  function transmitTelemetry(payload) {
    trackedEvents = [...trackedEvents, payload];

    if (fetchFn) {
      try {
        fetchFn(trackingEndpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
      } catch (e) {}
      return;
    }

    if (typeof navigator !== 'undefined' && typeof navigator.sendBeacon === 'function') {
      try {
        const blob = new Blob([JSON.stringify(payload)], { type: 'application/json' });
        if (navigator.sendBeacon(trackingEndpoint, blob)) return;
      } catch (err) {}
    }

    if (typeof window !== 'undefined' && typeof window.fetch === 'function') {
      try {
        window.fetch(trackingEndpoint, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload),
          keepalive: true
        }).catch(() => {});
      } catch (err) {}
    }
  }

  function filterLocalSamples() {
    const qLower = activeSearchQuery.toLowerCase();
    let filtered = sampleDocuments.filter(doc => {
      const matchQuery = !qLower ||
        doc.title.toLowerCase().includes(qLower) ||
        doc.summary.toLowerCase().includes(qLower) ||
        doc.category.toLowerCase().includes(qLower) ||
        doc.tags.some(t => t.toLowerCase().includes(qLower));

      const matchCategory = !selectedCategory || doc.category === selectedCategory;
      const matchTag = !selectedTag || doc.tags.includes(selectedTag);

      return matchQuery && matchCategory && matchTag;
    });

    if (sortBy === 'title') {
      filtered.sort((a, b) => sortOrder === 'asc' ? a.title.localeCompare(b.title) : b.title.localeCompare(a.title));
    } else if (sortBy === 'createdAt') {
      filtered.sort((a, b) => sortOrder === 'asc' ? new Date(a.createdAt) - new Date(b.createdAt) : new Date(b.createdAt) - new Date(a.createdAt));
    }

    searchResults = filtered;
    totalElements = filtered.length;
    searchCompleted = true;
    hasClickedDocument = false;
    isAbandoned = false;
    searchCompletedAt = nowFn();
    isLoading = false;
  }

  export function executeSearch(query = searchQuery) {
    searchQuery = query;
    activeSearchQuery = searchQuery.trim();
    searchError = null;

    filterLocalSamples();

    const fetcher = getEffectiveFetch();
    if (fetcher) {
      const params = new URLSearchParams();
      if (activeSearchQuery) params.set('q', activeSearchQuery);
      if (selectedCategory) params.set('category', selectedCategory);
      if (selectedTag) params.set('tags', selectedTag);
      params.set('sortBy', sortBy);
      params.set('sortOrder', sortOrder);

      const url = `${searchApiEndpoint}?${params.toString()}`;
      try {
        const res = fetcher(url, { method: 'GET' });
        if (res && typeof res.then === 'function') {
          res.then(async (r) => {
            if (r && r.ok && typeof r.json === 'function') {
              const data = await r.json();
              if (data && Array.isArray(data.items)) {
                searchResults = data.items;
                totalElements = data.pagination ? data.pagination.totalElements : data.items.length;
              }
            }
          }).catch(() => {});
        }
      } catch (e) {}
    }
  }

  export function handleDocumentClick(doc) {
    downloadError = null;
    failedDownloadDoc = null;

    if (searchCompleted && !hasClickedDocument) {
      const clickedTime = nowFn();
      const elapsedTimeMs = Math.max(0, Math.round(clickedTime - searchCompletedAt));
      hasClickedDocument = true;

      const eventPayload = {
        eventType: 'SEARCH_CLICK',
        query: activeSearchQuery,
        documentId: doc.id,
        documentTitle: doc.title,
        elapsedTimeMs: elapsedTimeMs,
        resultCount: searchResults.length,
        timestamp: new Date().toISOString()
      };

      transmitTelemetry(eventPayload);
    }
  }

  export async function handleDownloadFile(doc, simulateFail = false) {
    handleDocumentClick(doc);

    if (simulateFail) {
      downloadError = `Failed to download "${doc.title}". Network connection dropped.`;
      failedDownloadDoc = doc;
      return;
    }

    const fetcher = getEffectiveFetch();
    if (fetcher && doc.fileUrl) {
      try {
        const res = await fetcher(doc.fileUrl, { method: 'GET' });
        if (res && !res.ok) {
          throw new Error('Network error during file download');
        }
      } catch (err) {
        downloadError = `Failed to download "${doc.title}". ${err.message || 'Network error'}.`;
        failedDownloadDoc = doc;
      }
    } else if (doc.fileUrl && typeof window !== 'undefined') {
      try {
        window.open(doc.fileUrl, '_blank');
      } catch (err) {
        downloadError = `Failed to download "${doc.title}". Network connection dropped.`;
        failedDownloadDoc = doc;
      }
    }
  }

  export function retryDownload() {
    if (failedDownloadDoc) {
      const doc = failedDownloadDoc;
      downloadError = null;
      failedDownloadDoc = null;
      handleDownloadFile(doc, false);
    } else if (searchError) {
      executeSearch();
    }
  }

  export function handleAbandonment() {
    if (searchCompleted && !hasClickedDocument && !isAbandoned) {
      isAbandoned = true;

      const eventPayload = {
        eventType: 'SEARCH_ABANDONED',
        query: activeSearchQuery,
        resultCount: searchResults.length,
        executionTimeMs: 0,
        status: 'FAILED',
        timestamp: new Date().toISOString()
      };

      transmitTelemetry(eventPayload);
    }
  }

  export function resetFilters() {
    searchQuery = '';
    selectedCategory = '';
    selectedTag = '';
    sortBy = 'relevance';
    sortOrder = 'desc';
    downloadError = null;
    searchError = null;
    executeSearch('');
  }

  function onKeyDown(e) {
    if (e.key === 'Enter') {
      executeSearch(searchQuery);
    }
  }

  const unloadHandler = () => {
    handleAbandonment();
  };

  onMount(() => {
    executeSearch('');
  });

  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', unloadHandler);
    window.addEventListener('pagehide', unloadHandler);
  }

  onDestroy(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('beforeunload', unloadHandler);
      window.removeEventListener('pagehide', unloadHandler);
    }
    handleAbandonment();
  });
</script>

<!-- Outer Container -->
<div class="min-h-screen bg-background text-on-background font-body-md flex flex-col w-full relative">
  <!-- Top App Bar / Search Header -->
  <header class="bg-surface w-full sticky top-0 z-50 flat border-b border-outline-variant flex flex-col pt-4 pb-2 shadow-sm">
    <div class="flex items-center justify-between px-margin-mobile md:px-margin-desktop h-16 w-full gap-4 max-w-container-max mx-auto">
      <div class="flex items-center gap-3">
        <h1 class="font-headline-md text-headline-md font-bold text-primary tracking-tight">EPI Discovery</h1>
      </div>

      <!-- Search Bar -->
      <div class="flex-1 max-w-2xl relative">
        <label for="search-input" class="sr-only">Search epidemiological materials</label>
        <div class="relative w-full h-12 flex items-center group">
          <span class="material-symbols-outlined absolute left-3 text-outline pointer-events-none" aria-hidden="true">search</span>
          <input
            id="search-input"
            type="text"
            bind:value={searchQuery}
            on:keydown={onKeyDown}
            placeholder="Search epidemiological materials, protocols, datasets..."
            class="w-full h-full pl-10 pr-20 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary-fixed font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant transition-all duration-200"
            data-testid="search-input"
          />

          {#if searchQuery}
            <button
              type="button"
              aria-label="Clear search input"
              class="absolute right-12 text-outline hover:text-on-surface p-1 rounded-full hover:bg-surface-container-high transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
              on:click={() => { searchQuery = ''; executeSearch(''); }}
              data-testid="clear-search-btn"
            >
              <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">close</span>
            </button>
          {/if}

          <button
            type="button"
            aria-label="Submit search"
            class="absolute right-2 px-3 py-1 bg-primary text-on-primary font-label-md text-label-md rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
            on:click={() => executeSearch(searchQuery)}
            data-testid="search-submit"
          >
            Search
          </button>
        </div>
      </div>

      {#if searchCompleted && !hasClickedDocument}
        <button
          type="button"
          class="px-3 py-1.5 text-xs bg-error-container text-on-error-container font-label-md rounded-DEFAULT hover:opacity-90 focus:ring-2 focus:ring-error focus:outline-none transition-colors"
          on:click={handleAbandonment}
          data-testid="abandon-btn"
        >
          Abandon Search
        </button>
      {/if}
    </div>

    <!-- Filter Chips Bar -->
    <div class="px-margin-mobile md:px-margin-desktop pt-3 pb-2 overflow-x-auto hide-scrollbar max-w-container-max mx-auto w-full flex items-center gap-2">
      <span class="font-label-sm text-label-sm text-on-surface-variant mr-1 flex-shrink-0">Filter:</span>

      <button
        type="button"
        class={`px-3 py-1.5 rounded-full border font-label-md text-label-md transition-colors flex items-center gap-1 flex-shrink-0 focus:ring-2 focus:ring-primary focus:outline-none ${
          selectedCategory === '' ? 'bg-primary-container text-on-primary-container border-transparent' : 'bg-surface-container-lowest border-outline-variant text-on-surface-variant hover:bg-surface-container-high'
        }`}
        on:click={() => { selectedCategory = ''; executeSearch(); }}
        data-testid="filter-category-all"
      >
        All Types
      </button>

      <button
        type="button"
        class={`px-3 py-1.5 rounded-full border font-label-md text-label-md transition-colors flex items-center gap-1 flex-shrink-0 focus:ring-2 focus:ring-primary focus:outline-none ${
          selectedCategory === 'protocol' ? 'bg-primary-container text-on-primary-container border-transparent' : 'bg-surface-container-lowest border-outline-variant text-on-surface-variant hover:bg-surface-container-high'
        }`}
        on:click={() => { selectedCategory = 'protocol'; executeSearch(); }}
        data-testid="filter-category-protocol"
      >
        Protocols
      </button>

      <button
        type="button"
        class={`px-3 py-1.5 rounded-full border font-label-md text-label-md transition-colors flex items-center gap-1 flex-shrink-0 focus:ring-2 focus:ring-primary focus:outline-none ${
          selectedCategory === 'report' ? 'bg-primary-container text-on-primary-container border-transparent' : 'bg-surface-container-lowest border-outline-variant text-on-surface-variant hover:bg-surface-container-high'
        }`}
        on:click={() => { selectedCategory = 'report'; executeSearch(); }}
        data-testid="filter-category-report"
      >
        Reports
      </button>

      <button
        type="button"
        class={`px-3 py-1.5 rounded-full border font-label-md text-label-md transition-colors flex items-center gap-1 flex-shrink-0 focus:ring-2 focus:ring-primary focus:outline-none ${
          selectedCategory === 'dataset' ? 'bg-primary-container text-on-primary-container border-transparent' : 'bg-surface-container-lowest border-outline-variant text-on-surface-variant hover:bg-surface-container-high'
        }`}
        on:click={() => { selectedCategory = 'dataset'; executeSearch(); }}
        data-testid="filter-category-dataset"
      >
        Datasets
      </button>

      <button
        type="button"
        class={`px-3 py-1.5 rounded-full border font-label-md text-label-md transition-colors flex items-center gap-1 flex-shrink-0 focus:ring-2 focus:ring-primary focus:outline-none ${
          selectedCategory === 'guideline' ? 'bg-primary-container text-on-primary-container border-transparent' : 'bg-surface-container-lowest border-outline-variant text-on-surface-variant hover:bg-surface-container-high'
        }`}
        on:click={() => { selectedCategory = 'guideline'; executeSearch(); }}
        data-testid="filter-category-guideline"
      >
        Guidelines
      </button>

      {#if selectedTag}
        <button
          type="button"
          class="px-3 py-1.5 bg-secondary-container text-on-secondary-container rounded-full border border-transparent font-label-md text-label-md flex items-center gap-1 flex-shrink-0 hover:bg-surface-container-high transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
          on:click={() => { selectedTag = ''; executeSearch(); }}
          data-testid="clear-tag-btn"
        >
          Tag: {selectedTag} <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">close</span>
        </button>
      {/if}
    </div>
  </header>

  <!-- Error / Alert Banner -->
  {#if downloadError || searchError}
    <div class="px-margin-mobile md:px-margin-desktop pt-4 max-w-container-max mx-auto w-full" data-testid="error-banner">
      <div role="alert" class="p-4 bg-error-container border border-error text-on-error-container rounded-lg flex flex-col md:flex-row items-start md:items-center justify-between gap-3 shadow-sm">
        <div class="flex items-center gap-3">
          <span class="material-symbols-outlined text-error" aria-hidden="true">error</span>
          <div>
            <p class="font-body-md font-semibold">{downloadError || searchError}</p>
            <p class="font-body-sm text-xs text-on-surface-variant">Check your network connection or try again.</p>
          </div>
        </div>
        <button
          type="button"
          class="px-4 py-2 bg-error text-on-error font-label-md text-label-md rounded-DEFAULT hover:opacity-90 transition-opacity focus:ring-2 focus:ring-primary focus:outline-none flex-shrink-0"
          on:click={retryDownload}
          data-testid="retry-btn"
        >
          Retry Action
        </button>
      </div>
    </div>
  {/if}

  <!-- Main Content Canvas -->
  <main class="flex-1 overflow-y-auto px-margin-mobile md:px-margin-desktop py-density-comfortable pb-24 space-y-density-comfortable bg-surface-container-low max-w-container-max mx-auto w-full">
    <!-- Results Header / Controls -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-outline-variant pb-3">
      <div class="flex items-center gap-2">
        <span class="font-label-sm text-label-sm text-on-surface-variant" data-testid="result-count">
          Showing {searchResults.length} of {totalElements} materials
        </span>
        {#if activeSearchQuery}
          <span class="text-xs font-mono-data px-2 py-0.5 rounded bg-surface-container-highest text-on-surface-variant">
            Query: "{activeSearchQuery}"
          </span>
        {/if}
      </div>

      <div class="flex items-center gap-3">
        <label for="sort-select" class="font-label-sm text-label-sm text-on-surface-variant">Sort by:</label>
        <select
          id="sort-select"
          bind:value={sortBy}
          on:change={() => executeSearch()}
          class="bg-surface-container-lowest border border-outline-variant text-on-surface font-label-md text-label-md rounded-DEFAULT px-2 py-1 focus:ring-2 focus:ring-primary focus:outline-none"
          data-testid="sort-by-select"
        >
          <option value="relevance">Relevance</option>
          <option value="createdAt">Date Created</option>
          <option value="title">Title</option>
        </select>

        <button
          type="button"
          aria-label={`Toggle sort order, current: ${sortOrder}`}
          class="p-1 text-on-surface-variant hover:text-on-surface rounded border border-outline-variant bg-surface-container-lowest focus:ring-2 focus:ring-primary focus:outline-none"
          on:click={() => { sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'; executeSearch(); }}
          data-testid="sort-order-btn"
        >
          <span class="material-symbols-outlined" aria-hidden="true">
            {sortOrder === 'asc' ? 'arrow_upward' : 'arrow_downward'}
          </span>
        </button>
      </div>
    </div>

    <!-- Results List -->
    {#if isLoading}
      <div class="py-12 text-center text-on-surface-variant" data-testid="loading-state">
        <span class="material-symbols-outlined animate-spin text-primary text-3xl mb-2" aria-hidden="true">progress_activity</span>
        <p class="font-body-md">Searching epidemiological materials...</p>
      </div>
    {:else if searchResults.length > 0}
      <div class="space-y-4" data-testid="results-list">
        {#each searchResults as doc (doc.id)}
          <article
            class="bg-surface-container-lowest border border-outline-variant rounded-lg p-density-comfortable hover:border-primary transition-colors cursor-pointer group shadow-xs"
            data-testid="document-item"
            data-doc-id={doc.id}
          >
            <div class="flex justify-between items-start mb-2 gap-2">
              <div class="flex flex-wrap items-center gap-2">
                <span class="px-2 py-1 bg-secondary-fixed text-on-secondary-fixed font-label-sm text-label-sm rounded-DEFAULT inline-block uppercase font-bold tracking-wider">
                  {doc.category}
                </span>

                {#each doc.tags || [] as tag}
                  <button
                    type="button"
                    class="px-2 py-0.5 bg-surface-container-high text-on-surface-variant text-xs font-mono-data rounded hover:bg-surface-container-highest transition-colors focus:ring-1 focus:ring-primary focus:outline-none"
                    on:click={(e) => { e.stopPropagation(); selectedTag = tag; executeSearch(); }}
                    data-testid={`tag-chip-${tag}`}
                  >
                    #{tag}
                  </button>
                {/each}
              </div>

              <button
                type="button"
                aria-label={`Download document ${doc.title}`}
                class="px-3 py-1 bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary text-xs font-label-md rounded-DEFAULT transition-colors flex items-center gap-1 focus:ring-2 focus:ring-primary focus:outline-none flex-shrink-0"
                on:click|stopPropagation={() => handleDownloadFile(doc, false)}
                data-testid={`download-btn-${doc.id}`}
              >
                <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">download</span>
                Download
              </button>
            </div>

            <h3 class="font-headline-md text-headline-md text-on-surface mb-2 group-hover:text-primary transition-colors line-clamp-2">
              <button
                type="button"
                class="text-left w-full hover:underline focus:outline-none focus:ring-2 focus:ring-primary rounded"
                on:click={() => handleDocumentClick(doc)}
              >
                {doc.title}
              </button>
            </h3>

            <p class="font-body-sm text-body-sm text-on-surface-variant mb-3 line-clamp-2">
              {doc.summary || 'No detailed summary available.'}
            </p>

            <div class="flex flex-wrap items-center gap-2 font-label-sm text-label-sm text-on-secondary-container">
              {#if doc.author}
                <span class="flex items-center gap-1">
                  <span class="material-symbols-outlined" style="font-size: 14px;" aria-hidden="true">person</span>
                  <span class="truncate">{doc.author}</span>
                </span>
                <span class="w-1 h-1 rounded-full bg-outline mx-1" aria-hidden="true"></span>
              {/if}

              <span>Updated: {doc.updatedAt ? new Date(doc.updatedAt).toLocaleDateString() : 'N/A'}</span>
            </div>
          </article>
        {/each}
      </div>
    {:else}
      <!-- Explicit Empty State with Recovery Suggestions -->
      <div
        class="bg-surface-container-lowest border border-outline-variant rounded-lg p-8 text-center space-y-4 my-6"
        data-testid="empty-state"
      >
        <div class="w-16 h-16 bg-surface-container-high rounded-full flex items-center justify-center mx-auto text-outline">
          <span class="material-symbols-outlined text-3xl" aria-hidden="true">search_off</span>
        </div>

        <div>
          <h2 class="font-headline-md text-headline-md text-on-surface font-semibold mb-1">
            No materials found
          </h2>
          <p class="font-body-md text-on-surface-variant max-w-md mx-auto">
            We couldn't find any epidemiological materials matching <strong class="text-on-surface">"{activeSearchQuery}"</strong>.
          </p>
        </div>

        <div class="bg-surface-container-low p-4 rounded-lg max-w-lg mx-auto text-left space-y-2 border border-outline-variant">
          <h3 class="font-label-md text-label-md text-primary font-bold uppercase tracking-wider">
            Recovery Suggestions:
          </h3>
          <ul class="list-disc list-inside font-body-sm text-on-surface-variant space-y-1">
            <li>Check for spelling errors or try alternative disease names (e.g., "influenza" instead of "flu").</li>
            <li>Try broader keywords like "outbreak", "protocol", or "surveillance".</li>
            <li>Clear category or tag filters to search across all materials.</li>
            <li>Browse by popular material categories below.</li>
          </ul>
        </div>

        <div class="flex flex-wrap justify-center gap-3 pt-2">
          <button
            type="button"
            class="px-4 py-2 bg-primary text-on-primary font-label-md text-label-md rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
            on:click={resetFilters}
            data-testid="reset-filters-btn"
          >
            Reset Filters & Search
          </button>
        </div>
      </div>
    {/if}

    <!-- Browse / Quick Access Categories -->
    <section class="mt-8 border-t border-outline-variant pt-6" aria-labelledby="browse-heading">
      <h2 id="browse-heading" class="font-label-caps text-label-caps text-on-surface-variant uppercase tracking-wider mb-4 font-bold">
        Browse Materials by Category
      </h2>

      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
        <button
          type="button"
          class="bg-surface-container-lowest border border-outline-variant rounded-lg p-4 flex flex-col items-start gap-2 hover:border-primary hover:bg-surface-container transition-colors text-left focus:ring-2 focus:ring-primary focus:outline-none"
          on:click={() => { searchQuery = 'protocol'; executeSearch('protocol'); }}
        >
          <div class="p-2 bg-error-container text-on-error-container rounded-md">
            <span class="material-symbols-outlined" aria-hidden="true">picture_as_pdf</span>
          </div>
          <span class="font-body-md font-semibold text-on-surface">Protocols</span>
          <span class="font-label-sm text-on-surface-variant">Standard investigation procedures</span>
        </button>

        <button
          type="button"
          class="bg-surface-container-lowest border border-outline-variant rounded-lg p-4 flex flex-col items-start gap-2 hover:border-primary hover:bg-surface-container transition-colors text-left focus:ring-2 focus:ring-primary focus:outline-none"
          on:click={() => { searchQuery = 'report'; executeSearch('report'); }}
        >
          <div class="p-2 bg-secondary-fixed text-on-secondary-fixed rounded-md">
            <span class="material-symbols-outlined" aria-hidden="true">article</span>
          </div>
          <span class="font-body-md font-semibold text-on-surface">Reports</span>
          <span class="font-label-sm text-on-surface-variant">Surveillance & outbreak studies</span>
        </button>

        <button
          type="button"
          class="bg-surface-container-lowest border border-outline-variant rounded-lg p-4 flex flex-col items-start gap-2 hover:border-primary hover:bg-surface-container transition-colors text-left focus:ring-2 focus:ring-primary focus:outline-none"
          on:click={() => { searchQuery = 'dataset'; executeSearch('dataset'); }}
        >
          <div class="p-2 bg-tertiary-fixed text-on-tertiary-fixed rounded-md">
            <span class="material-symbols-outlined" aria-hidden="true">table_view</span>
          </div>
          <span class="font-body-md font-semibold text-on-surface">Datasets</span>
          <span class="font-label-sm text-on-surface-variant">Raw epidemiological data & CSVs</span>
        </button>

        <button
          type="button"
          class="bg-surface-container-lowest border border-outline-variant rounded-lg p-4 flex flex-col items-start gap-2 hover:border-primary hover:bg-surface-container transition-colors text-left focus:ring-2 focus:ring-primary focus:outline-none"
          on:click={() => { searchQuery = 'guideline'; executeSearch('guideline'); }}
        >
          <div class="p-2 bg-primary-fixed text-on-primary-fixed rounded-md">
            <span class="material-symbols-outlined" aria-hidden="true">description</span>
          </div>
          <span class="font-body-md font-semibold text-on-surface">Guidelines</span>
          <span class="font-label-sm text-on-surface-variant">Response & containment rules</span>
        </button>
      </div>
    </section>

    <!-- Telemetry Log for verification -->
    {#if trackedEvents.length > 0}
      <section class="mt-8 p-4 bg-surface-container border border-outline-variant rounded-lg" data-testid="telemetry-log">
        <h3 class="font-label-caps text-xs text-primary uppercase font-bold mb-2">Telemetry Events Logged ({trackedEvents.length})</h3>
        <div class="flex flex-col gap-2 font-mono-data text-xs">
          {#each trackedEvents as evt}
            <div class="p-2 bg-surface rounded border border-outline-variant flex flex-wrap gap-2 items-center">
              <span class="font-bold text-primary">[{evt.eventType}]</span>
              <span>Query: "{evt.query}"</span>
              {#if evt.elapsedTimeMs !== undefined}
                <span class="text-tertiary">Elapsed: {evt.elapsedTimeMs}ms</span>
              {/if}
              {#if evt.documentId}
                <span class="text-on-surface-variant">DocId: {evt.documentId.substring(0,8)}</span>
              {/if}
            </div>
          {/each}
        </div>
      </section>
    {/if}
  </main>

  <!-- Bottom Navigation Bar for Mobile -->
  <nav aria-label="Mobile navigation" class="bg-surface md:hidden fixed bottom-0 w-full z-50 flex justify-around items-center h-16 px-margin-mobile border-t border-outline-variant shadow-lg">
    <button type="button" class="flex flex-col items-center text-primary font-label-md text-xs focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">search</span>
      <span>Search</span>
    </button>
    <button type="button" class="flex flex-col items-center text-on-surface-variant hover:text-primary font-label-md text-xs focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" aria-hidden="true">history</span>
      <span>Recent</span>
    </button>
    <button type="button" class="flex flex-col items-center text-on-surface-variant hover:text-primary font-label-md text-xs focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" aria-hidden="true">bookmark</span>
      <span>Saved</span>
    </button>
  </nav>
</div>
