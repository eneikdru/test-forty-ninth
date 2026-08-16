<script>
  import { onMount } from 'svelte';

  export let apiEndpoint = '/api/v1/protocols';
  export let fetchFn = null;

  let searchQuery = '';
  let selectedCategory = '';
  let selectedStatus = '';
  let sortBy = 'createdAt';
  let sortOrder = 'desc';
  let page = 0;
  let size = 20;

  // Initial sample protocols list matching EpidemiologicalProtocol OpenAPI schema
  let sampleProtocols = [
    {
      id: 1,
      code: 'EPI-PROTO-001',
      title: 'COVID-19 Public Health Surveillance and Outbreak Investigation Protocol',
      category: 'Respiratory',
      version: 'v3.2',
      status: 'APPROVED',
      summary: 'Comprehensive guidance for standard case definitions, contact tracing, and outbreak investigation protocols for SARS-CoV-2.',
      authorOrganization: 'World Health Organization',
      publicationYear: 2022,
      createdAt: '2026-08-16T05:55:59Z'
    },
    {
      id: 2,
      code: 'EPI-PROTO-002',
      title: 'Dengue Vector Surveillance and Control Guidelines',
      category: 'Vector-Borne',
      version: 'v2.1',
      status: 'APPROVED',
      summary: 'Standardized field procedures for Aedes mosquito vector sampling, larvicidal application, and community containment strategies.',
      authorOrganization: 'Pan American Health Organization',
      publicationYear: 2023,
      createdAt: '2026-08-15T10:15:00Z'
    },
    {
      id: 3,
      code: 'EPI-PROTO-003',
      title: 'Cholera Rapid Response & Water Quality Protocol',
      category: 'Enteric',
      version: 'v1.0',
      status: 'DRAFT',
      summary: 'Emergency field response steps for acute watery diarrhea outbreaks and water point chlorination verification.',
      authorOrganization: 'Global Task Force on Cholera Control',
      publicationYear: 2024,
      createdAt: '2026-08-14T18:30:00Z'
    },
    {
      id: 4,
      code: 'EPI-PROTO-004',
      title: 'Avian Influenza A(H5N1) Human Contact Tracing Protocol',
      category: 'Zoonotic',
      version: 'v1.4',
      status: 'ARCHIVED',
      summary: 'Surveillance protocol for monitoring exposed poultry farm workers and secondary contact tracing.',
      authorOrganization: 'CDC Center for Emerging Zoonoses',
      publicationYear: 2021,
      createdAt: '2026-08-10T11:00:00Z'
    }
  ];

  let protocols = [...sampleProtocols];
  let pagination = {
    page: 0,
    size: 20,
    totalElements: sampleProtocols.length,
    totalPages: 1,
    isFirst: true,
    isLast: true
  };

  let isLoading = false;
  let errorMessage = null;

  function getEffectiveFetch() {
    if (fetchFn) return fetchFn;
    if (typeof window !== 'undefined' && typeof window.fetch === 'function') {
      return window.fetch.bind(window);
    }
    return null;
  }

  function filterLocalProtocols() {
    const qLower = searchQuery.trim().toLowerCase();
    let filtered = sampleProtocols.filter(p => {
      const matchQ = !qLower ||
        p.code.toLowerCase().includes(qLower) ||
        p.title.toLowerCase().includes(qLower) ||
        (p.summary && p.summary.toLowerCase().includes(qLower)) ||
        p.authorOrganization.toLowerCase().includes(qLower);

      const matchCat = !selectedCategory || p.category === selectedCategory;
      const matchStatus = !selectedStatus || p.status === selectedStatus;

      return matchQ && matchCat && matchStatus;
    });

    // Sorting
    filtered.sort((a, b) => {
      let valA = a[sortBy] ?? '';
      let valB = b[sortBy] ?? '';
      if (typeof valA === 'string') {
        const cmp = valA.localeCompare(valB);
        return sortOrder === 'asc' ? cmp : -cmp;
      }
      return sortOrder === 'asc' ? valA - valB : valB - valA;
    });

    protocols = filtered;
    pagination = {
      page: 0,
      size: size,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / size) || 1,
      isFirst: true,
      isLast: true
    };
  }

  export async function executeSearch(targetPage = 0) {
    page = targetPage;
    errorMessage = null;

    const fetcher = getEffectiveFetch();
    if (fetcher) {
      isLoading = true;
      const params = new URLSearchParams();
      if (searchQuery.trim()) params.set('q', searchQuery.trim());
      if (selectedCategory) params.set('category', selectedCategory);
      if (selectedStatus) params.set('status', selectedStatus);
      params.set('page', page.toString());
      params.set('size', size.toString());
      params.set('sortBy', sortBy);
      params.set('sortOrder', sortOrder);

      const url = `${apiEndpoint}?${params.toString()}`;
      try {
        const res = await fetcher(url, { method: 'GET' });
        if (res && res.ok) {
          const data = await res.json();
          if (data && Array.isArray(data.items)) {
            protocols = data.items;
            if (data.pagination) {
              pagination = data.pagination;
            } else {
              pagination = {
                page: page,
                size: size,
                totalElements: data.items.length,
                totalPages: 1,
                isFirst: page === 0,
                isLast: true
              };
            }
            isLoading = false;
            return;
          }
        } else if (res) {
          const errData = await res.json().catch(() => ({}));
          errorMessage = errData.message || `Failed to fetch protocols (${res.status})`;
        }
      } catch (err) {
        // Fallback to local filter if fetch fails
        filterLocalProtocols();
        isLoading = false;
        return;
      }
      isLoading = false;
    } else {
      filterLocalProtocols();
    }
  }

  function handleSearchSubmit(e) {
    if (e) e.preventDefault();
    executeSearch(0);
  }

  function resetFilters() {
    searchQuery = '';
    selectedCategory = '';
    selectedStatus = '';
    sortBy = 'createdAt';
    sortOrder = 'desc';
    executeSearch(0);
  }

  function getStatusBadgeStyle(status) {
    switch (status) {
      case 'APPROVED':
      case 'Active':
        return 'bg-primary-container/20 text-primary border-primary-container/30';
      case 'DRAFT':
      case 'Beta':
        return 'bg-tertiary-container/20 text-tertiary-container border-tertiary-container/30';
      case 'ARCHIVED':
      case 'Deprecated':
        return 'bg-error-container/20 text-error border-error-container/30';
      default:
        return 'bg-secondary-container/20 text-secondary border-secondary-container/30';
    }
  }

  onMount(() => {
    if (fetchFn) {
      executeSearch(0);
    }
  });
</script>

<div class="min-h-screen bg-background text-on-background font-body-md antialiased flex flex-col pt-12 pb-[72px] w-full">
  <!-- TopAppBar Header -->
  <header class="bg-surface dark:bg-surface-dim font-headline-sm border-b border-outline-variant dark:border-outline fixed top-0 left-0 w-full z-50 flex items-center px-md h-12 justify-between">
    <div class="flex items-center gap-xs text-primary dark:text-primary-fixed-dim p-1 rounded-full cursor-pointer hover:bg-surface-container-high" aria-hidden="true">
      <span class="material-symbols-outlined">search</span>
    </div>
    <h1 class="text-headline-sm font-headline-sm font-semibold text-on-surface dark:text-on-surface-variant flex-1 text-center">
      Epidemiological Protocols Search
    </h1>
    <div class="flex items-center gap-xs text-secondary dark:text-secondary-fixed-dim p-1 rounded-full cursor-pointer hover:bg-surface-container-high" aria-hidden="true">
      <span class="material-symbols-outlined">filter_list</span>
    </div>
  </header>

  <!-- Main Content Container -->
  <main class="flex-1 px-md py-lg w-full max-w-2xl mx-auto flex flex-col gap-md">
    <!-- Search Query Form -->
    <form on:submit={handleSearchSubmit} class="relative w-full group" role="search" aria-label="Search epidemiological protocols">
      <label for="protocol-search-input" class="sr-only">Search protocols</label>
      <div class="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-outline">
        <span class="material-symbols-outlined text-[20px]" aria-hidden="true">search</span>
      </div>
      <input
        id="protocol-search-input"
        type="text"
        bind:value={searchQuery}
        placeholder="Search protocols by title, code, summary, or author..."
        class="block w-full p-2.5 pl-10 pr-16 text-on-surface bg-surface border border-outline-variant rounded-lg font-body-md focus:ring-2 focus:ring-primary focus:border-primary outline-none placeholder-outline transition-shadow"
        data-testid="protocol-search-input"
      />
      <div class="absolute inset-y-0 right-0 flex items-center pr-2 gap-1">
        {#if searchQuery}
          <button
            type="button"
            aria-label="Clear search input"
            class="text-outline hover:text-on-surface p-1 rounded-full hover:bg-surface-container-high focus:outline-none focus:ring-2 focus:ring-primary"
            on:click={() => { searchQuery = ''; executeSearch(0); }}
            data-testid="clear-search-btn"
          >
            <span class="material-symbols-outlined text-[18px]" aria-hidden="true">close</span>
          </button>
        {/if}
        <button
          type="submit"
          aria-label="Submit search"
          class="bg-primary text-on-primary text-body-sm font-semibold px-3 py-1 rounded-md hover:bg-primary-container hover:text-on-primary-container focus:outline-none focus:ring-2 focus:ring-primary transition-colors"
          data-testid="search-submit-btn"
        >
          Search
        </button>
      </div>
    </form>

    <!-- Filters & Sorting Bar -->
    <div class="flex flex-wrap items-center justify-between gap-md p-3 bg-surface-container-low rounded-lg border border-outline-variant">
      <div class="flex flex-wrap items-center gap-sm">
        <label for="category-filter" class="font-label-caps text-label-caps text-secondary font-bold">Category:</label>
        <select
          id="category-filter"
          bind:value={selectedCategory}
          on:change={() => executeSearch(0)}
          class="bg-surface border border-outline-variant text-on-surface text-body-sm rounded px-2 py-1 focus:ring-2 focus:ring-primary outline-none"
          data-testid="category-filter-select"
        >
          <option value="">All Categories</option>
          <option value="Respiratory">Respiratory</option>
          <option value="Vector-Borne">Vector-Borne</option>
          <option value="Enteric">Enteric</option>
          <option value="Zoonotic">Zoonotic</option>
        </select>

        <label for="status-filter" class="font-label-caps text-label-caps text-secondary font-bold ml-2">Status:</label>
        <select
          id="status-filter"
          bind:value={selectedStatus}
          on:change={() => executeSearch(0)}
          class="bg-surface border border-outline-variant text-on-surface text-body-sm rounded px-2 py-1 focus:ring-2 focus:ring-primary outline-none"
          data-testid="status-filter-select"
        >
          <option value="">All Statuses</option>
          <option value="APPROVED">APPROVED</option>
          <option value="DRAFT">DRAFT</option>
          <option value="ARCHIVED">ARCHIVED</option>
        </select>
      </div>

      <div class="flex items-center gap-sm">
        <label for="sort-by-select" class="font-label-caps text-label-caps text-secondary font-bold">Sort:</label>
        <select
          id="sort-by-select"
          bind:value={sortBy}
          on:change={() => executeSearch(0)}
          class="bg-surface border border-outline-variant text-on-surface text-body-sm rounded px-2 py-1 focus:ring-2 focus:ring-primary outline-none"
          data-testid="sort-by-select"
        >
          <option value="createdAt">Date Created</option>
          <option value="title">Title</option>
          <option value="code">Protocol Code</option>
          <option value="publicationYear">Publication Year</option>
        </select>

        <button
          type="button"
          aria-label={`Toggle sort order, currently ${sortOrder}`}
          class="p-1 text-secondary hover:text-primary rounded border border-outline-variant bg-surface focus:ring-2 focus:ring-primary outline-none"
          on:click={() => { sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'; executeSearch(0); }}
          data-testid="toggle-sort-order-btn"
        >
          <span class="material-symbols-outlined text-[20px]" aria-hidden="true">
            {sortOrder === 'asc' ? 'arrow_upward' : 'arrow_downward'}
          </span>
        </button>
      </div>
    </div>

    <!-- Results Header / Counter -->
    <div class="flex justify-between items-center font-label-caps text-label-caps text-secondary px-1">
      <span data-testid="results-count-label">
        {pagination.totalElements} {pagination.totalElements === 1 ? 'PROTOCOL' : 'PROTOCOLS'} FOUND
      </span>
      {#if searchQuery || selectedCategory || selectedStatus}
        <button
          type="button"
          class="text-primary hover:underline font-semibold focus:outline-none focus:ring-2 focus:ring-primary rounded px-1"
          on:click={resetFilters}
          data-testid="reset-filters-btn"
        >
          Reset Filters
        </button>
      {/if}
    </div>

    <!-- Error Alert Banner -->
    {#if errorMessage}
      <div role="alert" class="p-4 bg-error-container text-on-error-container border border-error rounded-lg flex items-center justify-between gap-3">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-error" aria-hidden="true">error</span>
          <span class="font-body-sm font-semibold">{errorMessage}</span>
        </div>
        <button
          type="button"
          class="bg-error text-on-error px-3 py-1 rounded text-body-sm font-semibold hover:opacity-90 focus:ring-2 focus:ring-primary outline-none"
          on:click={() => executeSearch(page)}
          data-testid="retry-btn"
        >
          Retry
        </button>
      </div>
    {/if}

    <!-- Protocol Cards List -->
    {#if isLoading}
      <div class="py-12 text-center text-secondary flex flex-col items-center justify-center gap-2" data-testid="loading-spinner">
        <span class="material-symbols-outlined animate-spin text-primary text-3xl" aria-hidden="true">progress_activity</span>
        <span class="font-body-md font-medium">Searching protocols...</span>
      </div>
    {:else if protocols.length > 0}
      <div class="flex flex-col gap-sm" data-testid="protocols-list">
        {#each protocols as protocol (protocol.id || protocol.code)}
          <article
            class={`bg-surface border border-outline-variant rounded-lg p-lg hover:border-primary transition-colors flex flex-col gap-sm relative overflow-hidden group ${protocol.status === 'ARCHIVED' ? 'opacity-75' : ''}`}
            data-testid="protocol-card"
            data-protocol-id={protocol.id}
          >
            <div class="flex justify-between items-start gap-2">
              <div class="flex flex-col">
                <h2 class="font-headline-sm text-headline-sm text-on-surface group-hover:text-primary transition-colors flex items-center gap-2">
                  <span class={protocol.status === 'ARCHIVED' ? 'line-through decoration-outline-variant' : ''}>
                    {protocol.title}
                  </span>
                </h2>
                <div class="flex items-center gap-2 mt-1 font-label-mono text-label-mono text-secondary">
                  <span class="bg-surface-container px-1.5 py-0.5 rounded border border-outline-variant font-bold text-primary">
                    {protocol.code}
                  </span>
                  <span>{protocol.version}</span>
                  <span>•</span>
                  <span>{protocol.category}</span>
                </div>
              </div>

              <span class={`font-label-caps text-label-caps px-2 py-1 rounded-full border shrink-0 ${getStatusBadgeStyle(protocol.status)}`}>
                {protocol.status}
              </span>
            </div>

            {#if protocol.summary}
              <p class="font-body-sm text-body-sm text-on-surface-variant line-clamp-2 mt-1">
                {protocol.summary}
              </p>
            {/if}

            <div class="h-px bg-outline-variant w-full my-1"></div>

            <div class="flex justify-between items-center text-secondary font-body-sm text-body-sm">
              <span class="flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px]" aria-hidden="true">domain</span>
                <span class="truncate max-w-[200px]">{protocol.authorOrganization}</span>
                <span>({protocol.publicationYear})</span>
              </span>

              <button
                type="button"
                aria-label={`View details for ${protocol.title}`}
                class="flex items-center gap-1 text-primary hover:underline font-semibold focus:ring-2 focus:ring-primary focus:outline-none rounded px-1 py-0.5"
              >
                <span class="material-symbols-outlined text-[18px]" aria-hidden="true">visibility</span>
                View Details
              </button>
            </div>
          </article>
        {/each}
      </div>

      <!-- Pagination Controls -->
      {#if pagination.totalPages > 1}
        <div class="flex justify-between items-center pt-md border-t border-outline-variant font-body-sm">
          <button
            type="button"
            disabled={pagination.isFirst || page === 0}
            class="px-3 py-1.5 rounded border border-outline-variant bg-surface text-on-surface disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-container-high focus:ring-2 focus:ring-primary outline-none"
            on:click={() => executeSearch(page - 1)}
            data-testid="prev-page-btn"
          >
            Previous
          </button>

          <span class="font-label-caps text-secondary">
            Page {page + 1} of {pagination.totalPages}
          </span>

          <button
            type="button"
            disabled={pagination.isLast || page >= pagination.totalPages - 1}
            class="px-3 py-1.5 rounded border border-outline-variant bg-surface text-on-surface disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-container-high focus:ring-2 focus:ring-primary outline-none"
            on:click={() => executeSearch(page + 1)}
            data-testid="next-page-btn"
          >
            Next
          </button>
        </div>
      {/if}
    {:else}
      <!-- Empty State -->
      <div class="bg-surface border border-outline-variant rounded-lg p-lg text-center flex flex-col items-center gap-md my-md" data-testid="empty-state">
        <span class="material-symbols-outlined text-4xl text-outline" aria-hidden="true">search_off</span>
        <div class="flex flex-col gap-xs">
          <h2 class="font-headline-sm text-headline-sm text-on-surface font-semibold">No protocols found</h2>
          <p class="font-body-sm text-secondary">
            No epidemiological protocols match your search query or selected filters.
          </p>
        </div>
        <button
          type="button"
          class="bg-primary text-on-primary px-4 py-2 rounded-lg font-label-caps text-label-caps hover:bg-primary-container hover:text-on-primary-container focus:ring-2 focus:ring-primary outline-none transition-colors"
          on:click={resetFilters}
          data-testid="empty-reset-btn"
        >
          Clear All Filters
        </button>
      </div>
    {/if}
  </main>

  <!-- BottomNavBar for Mobile Navigation -->
  <nav aria-label="Mobile navigation" class="bg-surface dark:bg-surface-dim font-label-caps text-label-caps font-bold border-t border-outline-variant dark:border-outline fixed bottom-0 left-0 w-full z-50 flex justify-around items-center px-margin py-sm md:hidden">
    <button type="button" class="flex flex-col items-center justify-center bg-secondary-container text-on-secondary-container rounded-full px-sm py-xs focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">search</span>
      <span>Protocols</span>
    </button>
    <button type="button" class="flex flex-col items-center justify-center text-on-surface-variant px-sm py-xs hover:bg-surface-container-low rounded-full focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" aria-hidden="true">history</span>
      <span>History</span>
    </button>
    <button type="button" class="flex flex-col items-center justify-center text-on-surface-variant px-sm py-xs hover:bg-surface-container-low rounded-full focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" aria-hidden="true">bookmark</span>
      <span>Saved</span>
    </button>
  </nav>
</div>
