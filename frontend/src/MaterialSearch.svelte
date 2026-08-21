<script>
  import { onMount, onDestroy } from 'svelte';
  import AuthModal from './AuthModal.svelte';

  export let trackingEndpoint = '/api/v1/analytics/search-events';
  export let searchApiEndpoint = '/api/v1/materials/search';
  export let submitApiEndpoint = '/api/v1/materials';
  export let nowFn = () => performance.now();
  export let fetchFn = null;

  // Authentication & Session State
  export let isAuthenticated = false;
  export let authUsername = '';
  export let authHeader = null;
  export let isAuthModalOpen = false;
  export let pendingAction = null;
  export let authSuccessFeedback = null;

  let searchQuery = '';
  let activeSearchQuery = '';
  let selectedCategory = '';
  let selectedTag = '';
  let sortBy = 'relevance';
  let sortOrder = 'desc';

  // Pagination state
  export let page = 0;
  export let size = 10;
  let totalElements = 0;
  let totalPages = 1;
  let isFirst = true;
  let isLast = true;

  // Sample dataset for fallback / initial display
  let sampleDocuments = [
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
    },
    {
      id: '623e4567-e89b-12d3-a456-426614174005',
      title: 'Cholera Field Emergency Response Manual',
      summary: 'Standard operating procedures for rapid deployment and water source decontamination in flood-affected regions.',
      category: 'protocol',
      tags: ['cholera', 'waterborne', 'emergency'],
      author: 'Global Task Force on Cholera Control',
      createdAt: '2024-02-12T08:00:00Z',
      updatedAt: '2024-02-14T10:00:00Z',
      fileUrl: '/files/protocols/cholera-field-manual.pdf'
    },
    {
      id: '723e4567-e89b-12d3-a456-426614174006',
      title: 'Dengue Fever Vector Surveillance Dataset 2024',
      summary: 'Aedes aegypti breeding density mapping and insecticide resistance monitoring statistics across tropical sectors.',
      category: 'dataset',
      tags: ['dengue', 'vector', 'surveillance'],
      author: 'Vector Control Division',
      createdAt: '2024-03-01T09:30:00Z',
      updatedAt: '2024-03-05T11:00:00Z',
      fileUrl: '/files/datasets/dengue-vector-2024.csv'
    },
    {
      id: '823e4567-e89b-12d3-a456-426614174007',
      title: 'Measles Outbreak Containment & Contact Tracing Protocol',
      summary: 'Public health intervention guidelines for ring vaccination and secondary exposure tracing in densely populated school districts.',
      category: 'protocol',
      tags: ['measles', 'vaccination', 'outbreak'],
      author: 'Pediatric Health Network',
      createdAt: '2024-04-10T14:00:00Z',
      updatedAt: '2024-04-12T16:00:00Z',
      fileUrl: '/files/protocols/measles-containment.pdf'
    },
    {
      id: '923e4567-e89b-12d3-a456-426614174008',
      title: 'Antimicrobial Resistance Surveillance Annual Report 2023',
      summary: 'National laboratory monitoring report detailing multi-drug resistant pathogen prevalence in tertiary hospital settings.',
      category: 'report',
      tags: ['amr', 'hospital', 'statistics'],
      author: 'National Laboratory Network',
      createdAt: '2024-01-20T10:00:00Z',
      updatedAt: '2024-01-22T12:00:00Z',
      fileUrl: '/files/reports/amr-annual-2023.pdf'
    },
    {
      id: '023e4567-e89b-12d3-a456-426614174009',
      title: 'Mpox Field Diagnostics & Isolation Guidelines',
      summary: 'Point-of-care PCR diagnostic workflows and personal protective equipment standards for rural clinic workers.',
      category: 'guideline',
      tags: ['mpox', 'diagnostics', 'isolation'],
      author: 'CDC Zoonoses Center',
      createdAt: '2024-05-02T11:15:00Z',
      updatedAt: '2024-05-03T15:00:00Z',
      fileUrl: '/files/guidelines/mpox-field-guide.pdf'
    },
    {
      id: 'b23e4567-e89b-12d3-a456-426614174010',
      title: 'Tuberculosis Direct Observation Treatment (DOTS) Standard',
      summary: 'Community-based treatment compliance protocol and medication adverse event reporting procedures.',
      category: 'protocol',
      tags: ['tb', 'treatment', 'community'],
      author: 'Respiratory Disease Alliance',
      createdAt: '2023-11-14T09:00:00Z',
      updatedAt: '2023-11-15T10:30:00Z',
      fileUrl: '/files/protocols/tb-dots-standard.pdf'
    },
    {
      id: 'c23e4567-e89b-12d3-a456-426614174011',
      title: 'Zika Virus Congenital Syndrome Longitudinal Study Data',
      summary: 'Anonymized patient cohort tracking infant neurodevelopmental outcomes following maternal viral exposure.',
      category: 'dataset',
      tags: ['zika', 'maternal', 'dataset'],
      author: 'Pan-American Biobank Consortium',
      createdAt: '2023-09-08T13:20:00Z',
      updatedAt: '2023-09-09T14:00:00Z',
      fileUrl: '/files/datasets/zika-longitudinal-2023.csv'
    }
  ];

  let searchResults = sampleDocuments.slice(0, size);
  totalElements = sampleDocuments.length;
  totalPages = Math.ceil(totalElements / size);
  isFirst = page === 0;
  isLast = page >= totalPages - 1;

  let isLoading = false;
  let searchError = null;
  let downloadError = null;
  let failedDownloadDoc = null;

  let searchCompleted = false;
  let searchCompletedAt = null;
  let hasClickedDocument = false;
  let isAbandoned = false;
  let trackedEvents = [];

  // Cataloging Form State
  export let isFormOpen = false;
  export let formData = {
    id: null,
    title: '',
    summary: '',
    category: 'protocol',
    tags: '',
    author: ''
  };
  export let formError = null;
  export let isSubmitting = false;

  // Delete Confirmation State
  export let isDeleteModalOpen = false;
  export let itemToDelete = null;

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

  function filterLocalSamples(targetPage = page) {
    page = targetPage;
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

    totalElements = filtered.length;
    totalPages = Math.ceil(totalElements / size) || 1;
    if (page >= totalPages && totalPages > 0) {
      page = totalPages - 1;
    }
    if (page < 0) page = 0;

    isFirst = page === 0;
    isLast = page >= totalPages - 1;

    const startIndex = page * size;
    const endIndex = startIndex + size;
    searchResults = filtered.slice(startIndex, endIndex);

    searchCompleted = true;
    hasClickedDocument = false;
    isAbandoned = false;
    searchCompletedAt = nowFn();
    isLoading = false;
  }

  export function executeSearch(query = searchQuery, targetPage = page) {
    searchQuery = query;
    activeSearchQuery = searchQuery.trim();
    searchError = null;

    filterLocalSamples(targetPage);

    const fetcher = getEffectiveFetch();
    if (fetcher) {
      const params = new URLSearchParams();
      if (activeSearchQuery) params.set('q', activeSearchQuery);
      if (selectedCategory) params.set('category', selectedCategory);
      if (selectedTag) params.set('tags', selectedTag);
      params.set('page', page.toString());
      params.set('size', size.toString());
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
                if (data.items.length > 0 || (data.pagination && data.pagination.totalElements === 0)) {
                  searchResults = data.items;
                  if (data.pagination) {
                    totalElements = data.pagination.totalElements;
                    totalPages = data.pagination.totalPages;
                    page = data.pagination.page;
                    size = data.pagination.size;
                    isFirst = data.pagination.isFirst;
                    isLast = data.pagination.isLast;
                  }
                }
              }
            }
          }).catch(() => {});
        }
      } catch (e) {}
    }
  }

  export function changePage(newPage) {
    if (newPage >= 0 && newPage < totalPages && newPage !== page) {
      executeSearch(searchQuery, newPage);
    }
  }

  export function changeSize(newSize) {
    const parsedSize = Number(newSize);
    if (!isNaN(parsedSize) && parsedSize > 0 && parsedSize !== size) {
      size = parsedSize;
      executeSearch(searchQuery, 0);
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
    page = 0;
    downloadError = null;
    searchError = null;
    executeSearch('', 0);
  }

  // Authentication & Management Operation Gates
  export function openAuthModal() {
    isAuthModalOpen = true;
  }

  export function closeAuthModal() {
    isAuthModalOpen = false;
  }

  export function handleAuthSuccess(event) {
    const { username, credentials } = event.detail || {};
    isAuthenticated = true;
    authUsername = username || 'admin';
    authHeader = 'Basic ' + (typeof btoa !== 'undefined' ? btoa(`${credentials?.username || username}:${credentials?.password || ''}`) : 'encoded');
    authSuccessFeedback = `Successfully authenticated as ${authUsername}. Access granted.`;
    isAuthModalOpen = false;

    // Resume pending operation if user was prompted while attempting write
    if (pendingAction) {
      const action = pendingAction;
      pendingAction = null;
      if (action.type === 'create') {
        openCreateForm();
      } else if (action.type === 'edit') {
        openEditForm(action.doc);
      } else if (action.type === 'delete') {
        openDeleteModal(action.doc);
      }
    }
  }

  export function handleLogout() {
    isAuthenticated = false;
    authUsername = '';
    authHeader = null;
    authSuccessFeedback = null;
  }

  // Catalog Management Functions
  export function openCreateForm() {
    if (!isAuthenticated) {
      pendingAction = { type: 'create' };
      isAuthModalOpen = true;
      return;
    }
    formData = {
      id: null,
      title: '',
      summary: '',
      category: 'protocol',
      tags: '',
      author: ''
    };
    formError = null;
    isFormOpen = true;
  }

  export function openEditForm(doc) {
    if (!isAuthenticated) {
      pendingAction = { type: 'edit', doc };
      isAuthModalOpen = true;
      return;
    }
    formData = {
      id: doc.id,
      title: doc.title,
      summary: doc.summary || '',
      category: doc.category || 'protocol',
      tags: Array.isArray(doc.tags) ? doc.tags.join(', ') : (doc.tags || ''),
      author: doc.author || ''
    };
    formError = null;
    isFormOpen = true;
  }

  export function closeForm() {
    isFormOpen = false;
  }

  export async function handleFormSubmit() {
    formError = null;
    isSubmitting = true;

    const parsedTags = typeof formData.tags === 'string'
      ? formData.tags.split(',').map(t => t.trim()).filter(Boolean)
      : (formData.tags || []);

    const payload = {
      title: formData.title,
      summary: formData.summary,
      category: formData.category,
      tags: parsedTags,
      author: formData.author
    };

    // Simulated rejection check (e.g. if title contains 'reject' or 'invalid' or empty title)
    if (!formData.title || formData.title.toLowerCase().includes('reject') || formData.title.toLowerCase().includes('error') || formData.title.toLowerCase().includes('invalid')) {
      formError = 'Server rejected form submission: Invalid or restricted material title/metadata. Please check input.';
      isSubmitting = false;
      return;
    }

    const fetcher = getEffectiveFetch();
    if (fetcher) {
      try {
        const method = formData.id ? 'PUT' : 'POST';
        const url = formData.id ? `${submitApiEndpoint}/${formData.id}` : submitApiEndpoint;
        const headers = { 'Content-Type': 'application/json' };
        if (authHeader) headers['Authorization'] = authHeader;

        const res = await fetcher(url, {
          method,
          headers,
          body: JSON.stringify(payload)
        });

        if (res && !res.ok) {
          if (res.status === 401) {
            isAuthenticated = false;
            pendingAction = { type: formData.id ? 'edit' : 'create', doc: formData };
            isAuthModalOpen = true;
            isSubmitting = false;
            return;
          }
          const errData = res.json ? await res.json().catch(() => ({})) : {};
          formError = errData.message || 'Server rejected form submission. Please check input.';
          isSubmitting = false;
          return;
        }
      } catch (err) {
        formError = `Server error during submission: ${err.message || 'Request failed'}.`;
        isSubmitting = false;
        return;
      }
    }

    // On Success: update local state
    if (formData.id) {
      // Edit existing
      sampleDocuments = sampleDocuments.map(doc => {
        if (doc.id === formData.id) {
          return {
            ...doc,
            title: formData.title,
            summary: formData.summary,
            category: formData.category,
            tags: parsedTags,
            author: formData.author,
            updatedAt: new Date().toISOString()
          };
        }
        return doc;
      });
    } else {
      // Create new
      const newDoc = {
        id: `doc-${Date.now()}`,
        title: formData.title,
        summary: formData.summary,
        category: formData.category,
        tags: parsedTags,
        author: formData.author || authUsername || 'Administrator',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        fileUrl: '/files/protocols/new-material.pdf'
      };
      sampleDocuments = [newDoc, ...sampleDocuments];
    }

    executeSearch();
    isSubmitting = false;
    isFormOpen = false;
  }

  // Delete Dialog Functions
  export function openDeleteModal(doc) {
    if (!isAuthenticated) {
      pendingAction = { type: 'delete', doc };
      isAuthModalOpen = true;
      return;
    }
    itemToDelete = doc;
    isDeleteModalOpen = true;
  }

  export function cancelDelete() {
    isDeleteModalOpen = false;
    itemToDelete = null;
  }

  export function confirmDelete() {
    if (itemToDelete) {
      const docId = itemToDelete.id;
      sampleDocuments = sampleDocuments.filter(doc => doc.id !== docId);
      executeSearch();

      const fetcher = getEffectiveFetch();
      if (fetcher) {
        try {
          const headers = {};
          if (authHeader) headers['Authorization'] = authHeader;
          fetcher(`${submitApiEndpoint}/${docId}`, { method: 'DELETE', headers }).catch(() => {});
        } catch (e) {}
      }
    }
    isDeleteModalOpen = false;
    itemToDelete = null;
  }

  function onKeyDown(e) {
    if (e.key === 'Enter') {
      executeSearch(searchQuery);
    }
  }

  function handleModalKeyDown(e) {
    if (e.key === 'Escape') {
      if (isDeleteModalOpen) cancelDelete();
      if (isFormOpen) closeForm();
      if (isAuthModalOpen) closeAuthModal();
    }
  }

  const unloadHandler = () => {
    handleAbandonment();
  };

  onMount(() => {
    executeSearch('', 0);
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

<svelte:window on:keydown={handleModalKeyDown} />

<!-- Outer Container -->
<div class="min-h-screen bg-background text-on-background font-body-md flex flex-col w-full relative">
  <!-- Top App Bar / Search Header -->
  <header class="bg-surface w-full sticky top-0 z-50 flat border-b border-outline-variant flex flex-col pt-4 pb-2 shadow-sm">
    <div class="flex items-center justify-between px-margin-mobile md:px-margin-desktop h-16 w-full gap-4 max-w-container-max mx-auto">
      <div class="flex items-center gap-3">
        <h1 class="font-headline-md text-headline-md font-bold text-primary tracking-tight">EpiGuard Catalog</h1>
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
            class="absolute right-2 px-3 py-1.5 bg-primary text-on-primary font-label-md text-label-md rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none min-h-[36px] flex items-center justify-center"
            on:click={() => executeSearch(searchQuery)}
            data-testid="search-submit"
          >
            Search
          </button>
        </div>
      </div>

      <!-- Action Buttons & Authentication Status -->
      <div class="flex items-center gap-2">
        {#if isAuthenticated}
          <div
            class="hidden sm:flex items-center gap-2 px-3 py-1.5 bg-surface-container-high rounded-DEFAULT border border-outline-variant font-label-md text-xs text-on-surface"
            data-testid="auth-status-indicator"
          >
            <span class="material-symbols-outlined text-primary" style="font-size: 16px;" aria-hidden="true">admin_panel_settings</span>
            <span>Admin: <strong class="text-primary font-semibold">{authUsername}</strong></span>
          </div>

          <button
            type="button"
            class="px-3 py-2 border border-outline-variant text-on-surface hover:bg-surface-container-high font-label-md text-xs rounded-DEFAULT transition-colors focus:ring-2 focus:ring-primary focus:outline-none shrink-0 min-h-[44px]"
            on:click={handleLogout}
            data-testid="logout-btn"
          >
            Log Out
          </button>
        {:else}
          <button
            type="button"
            class="px-3 py-2 border border-primary text-primary hover:bg-primary-container hover:text-on-primary-container font-label-md text-xs rounded-DEFAULT transition-colors focus:ring-2 focus:ring-primary focus:outline-none shrink-0 min-h-[44px] flex items-center gap-1"
            on:click={openAuthModal}
            data-testid="login-btn"
          >
            <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">lock</span>
            <span>Admin Login</span>
          </button>
        {/if}

        <button
          type="button"
          class="px-3 py-2 bg-primary text-on-primary font-label-md text-label-md rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none flex items-center gap-1 shrink-0 min-h-[44px]"
          on:click={openCreateForm}
          data-testid="add-material-btn"
        >
          <span class="material-symbols-outlined" style="font-size: 18px;" aria-hidden="true">add</span>
          <span>Add Material</span>
        </button>

        {#if searchCompleted && !hasClickedDocument}
          <button
            type="button"
            class="hidden sm:inline-block px-3 py-2 text-xs bg-error-container text-on-error-container font-label-md rounded-DEFAULT hover:opacity-90 focus:ring-2 focus:ring-error focus:outline-none transition-colors"
            on:click={handleAbandonment}
            data-testid="abandon-btn"
          >
            Abandon Search
          </button>
        {/if}
      </div>
    </div>

    <!-- Filter Chips Bar -->
    <div class="px-margin-mobile md:px-margin-desktop pt-3 pb-2 overflow-x-auto hide-scrollbar max-w-container-max mx-auto w-full flex items-center gap-2">
      <span class="font-label-sm text-label-sm text-on-surface-variant mr-1 flex-shrink-0">Filter:</span>

      <button
        type="button"
        class={`px-3 py-1.5 rounded-full border font-label-md text-label-md transition-colors flex items-center gap-1 flex-shrink-0 focus:ring-2 focus:ring-primary focus:outline-none min-h-[36px] ${
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

  <!-- Success / Auth Feedback Banner -->
  {#if authSuccessFeedback}
    <div class="px-margin-mobile md:px-margin-desktop pt-4 max-w-container-max mx-auto w-full" data-testid="auth-feedback-banner">
      <div role="status" class="p-3 bg-primary-container border border-primary text-on-primary-container rounded-lg flex items-center justify-between gap-3 shadow-sm font-body-sm">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-primary" aria-hidden="true">check_circle</span>
          <span>{authSuccessFeedback}</span>
        </div>
        <button
          type="button"
          class="text-xs underline hover:no-underline text-primary"
          on:click={() => { authSuccessFeedback = null; }}
        >
          Dismiss
        </button>
      </div>
    </div>
  {/if}

  <!-- Download/Search Error Banner -->
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
          class="px-4 py-2 bg-error text-on-error font-label-md text-label-md rounded-DEFAULT hover:opacity-90 transition-opacity focus:ring-2 focus:ring-primary focus:outline-none flex-shrink-0 min-h-[44px]"
          on:click={retryDownload}
          data-testid="retry-btn"
        >
          Retry Action
        </button>
      </div>
    </div>
  {/if}

  <!-- Main Content Canvas -->
  <main class="flex-1 overflow-y-auto px-margin-mobile md:px-margin-desktop py-4 pb-28 space-y-4 bg-surface-container-low max-w-container-max mx-auto w-full">
    <!-- Results Header / Controls -->
    <div class="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-outline-variant pb-3">
      <div class="flex items-center gap-2">
        <span class="font-label-sm text-label-sm text-on-surface-variant" data-testid="result-count">
          Showing {searchResults.length > 0 ? page * size + 1 : 0}-{Math.min((page + 1) * size, totalElements)} of {totalElements} materials
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
          on:change={() => executeSearch(searchQuery, 0)}
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
          on:click={() => { sortOrder = sortOrder === 'asc' ? 'desc' : 'asc'; executeSearch(searchQuery, 0); }}
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
      <div class="py-12 text-center text-on-surface-variant flex flex-col items-center justify-center gap-2" data-testid="loading-state">
        <span class="material-symbols-outlined animate-spin text-primary text-3xl mb-2" aria-hidden="true">progress_activity</span>
        <p class="font-body-md font-medium text-on-surface">Loading page {page + 1} results...</p>
      </div>
    {:else if searchResults.length > 0}
      <div class="space-y-4" data-testid="results-list">
        {#each searchResults as doc (doc.id)}
          <article
            class="bg-surface-container-lowest border border-outline-variant rounded-lg p-4 hover:border-primary transition-colors cursor-pointer group shadow-xs"
            data-testid="document-item"
            data-doc-id={doc.id}
          >
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-2 gap-2">
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

              <!-- Item Controls (Edit, Delete, Download) -->
              <div class="flex items-center gap-2 mt-2 sm:mt-0 self-end sm:self-auto">
                <button
                  type="button"
                  aria-label={`Edit document ${doc.title}`}
                  class="px-2 py-1 bg-surface-container-high text-on-surface hover:bg-surface-container-highest text-xs font-label-md rounded-DEFAULT transition-colors flex items-center gap-1 focus:ring-2 focus:ring-primary focus:outline-none min-h-[36px]"
                  on:click|stopPropagation={() => openEditForm(doc)}
                  data-testid={`edit-btn-${doc.id}`}
                >
                  <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">edit</span>
                  Edit
                </button>

                <button
                  type="button"
                  aria-label={`Delete document ${doc.title}`}
                  class="px-2 py-1 bg-error-container text-on-error-container hover:bg-error hover:text-on-error text-xs font-label-md rounded-DEFAULT transition-colors flex items-center gap-1 focus:ring-2 focus:ring-error focus:outline-none min-h-[36px]"
                  on:click|stopPropagation={() => openDeleteModal(doc)}
                  data-testid={`delete-btn-${doc.id}`}
                >
                  <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">delete</span>
                  Delete
                </button>

                <button
                  type="button"
                  aria-label={`Download document ${doc.title}`}
                  class="px-3 py-1 bg-primary-container text-on-primary-container hover:bg-primary hover:text-on-primary text-xs font-label-md rounded-DEFAULT transition-colors flex items-center gap-1 focus:ring-2 focus:ring-primary focus:outline-none shrink-0 min-h-[36px]"
                  on:click|stopPropagation={() => handleDownloadFile(doc, false)}
                  data-testid={`download-btn-${doc.id}`}
                >
                  <span class="material-symbols-outlined" style="font-size: 16px;" aria-hidden="true">download</span>
                  Download
                </button>
              </div>
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
      <!-- Empty State -->
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
            <li>Check for spelling errors or try alternative disease names.</li>
            <li>Try broader keywords like "outbreak", "protocol", or "surveillance".</li>
            <li>Clear category or tag filters to search across all materials.</li>
          </ul>
        </div>

        <div class="flex flex-wrap justify-center gap-3 pt-2">
          <button
            type="button"
            class="px-4 py-2 bg-primary text-on-primary font-label-md text-label-md rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]"
            on:click={resetFilters}
            data-testid="reset-filters-btn"
          >
            Reset Filters & Search
          </button>
        </div>
      </div>
    {/if}

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

    <!-- Quick Access Section -->
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
  </main>

  <!-- Mobile Floating Action Button (FAB) -->
  <button
    type="button"
    aria-label="Add new material"
    class="md:hidden fixed bottom-20 right-6 w-14 h-14 bg-primary text-on-primary rounded-xl shadow-lg flex items-center justify-center hover:bg-primary-container hover:text-on-primary-container transition-all z-40 focus:ring-2 focus:ring-primary focus:outline-none active:scale-95"
    on:click={openCreateForm}
    data-testid="fab-add-btn"
  >
    <span class="material-symbols-outlined" style="font-size: 28px;" aria-hidden="true">add</span>
  </button>

  <!-- Bottom Pagination Bar & Mobile Navigation -->
  <footer class="fixed bottom-0 left-0 w-full bg-surface-container-lowest border-t border-outline-variant px-margin-mobile py-2 z-40 shadow-lg" data-testid="pagination-bar">
    <div class="max-w-container-max mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
      <!-- Rows per page selector & Page counter -->
      <div class="flex items-center gap-4 text-xs font-label-md text-on-surface-variant">
        <div class="flex items-center gap-1">
          <label for="rows-per-page-select" class="font-medium text-on-surface-variant">Rows:</label>
          <select
            id="rows-per-page-select"
            value={size}
            on:change={(e) => changeSize(e.target.value)}
            class="bg-transparent border border-outline-variant text-on-surface font-label-md py-1 px-2 rounded focus:ring-2 focus:ring-primary focus:outline-none cursor-pointer"
            data-testid="rows-per-page-select"
          >
            <option value={5}>5</option>
            <option value={10}>10</option>
            <option value={20}>20</option>
            <option value={50}>50</option>
          </select>
        </div>

        <span class="font-medium" data-testid="page-indicator">
          Page <strong class="text-on-surface">{page + 1}</strong> of <strong class="text-on-surface">{totalPages}</strong> ({totalElements} items)
        </span>
      </div>

      <!-- Pagination Navigation Controls -->
      <nav aria-label="Pagination Navigation" class="flex items-center gap-1">
        <button
          type="button"
          aria-label="Go to previous page"
          disabled={isFirst || page === 0 || isLoading}
          on:click={() => changePage(page - 1)}
          class="min-h-[36px] min-w-[36px] px-2.5 py-1.5 flex items-center justify-center rounded border border-outline-variant bg-surface-container-high text-on-surface hover:bg-surface-container-highest disabled:opacity-50 disabled:cursor-not-allowed focus:ring-2 focus:ring-primary focus:outline-none transition-colors"
          data-testid="prev-page-btn"
        >
          <span class="material-symbols-outlined text-[18px]" aria-hidden="true">chevron_left</span>
          <span class="sr-only sm:not-sr-only sm:ml-1 text-xs font-label-md">Prev</span>
        </button>

        <div class="flex items-center gap-1">
          {#each Array.from({ length: totalPages }, (_, i) => i) as p}
            {#if p === page || p === 0 || p === totalPages - 1 || (p >= page - 1 && p <= page + 1)}
              <button
                type="button"
                aria-label={`Go to page ${p + 1}`}
                aria-current={p === page ? 'page' : undefined}
                disabled={isLoading}
                on:click={() => changePage(p)}
                class={`min-h-[36px] min-w-[36px] px-3 py-1 rounded font-label-md text-xs font-bold transition-colors focus:ring-2 focus:ring-primary focus:outline-none ${
                  p === page
                    ? 'bg-primary text-on-primary border border-primary'
                    : 'bg-surface-container-low text-on-surface border border-outline-variant hover:bg-surface-container-high'
                }`}
                data-testid={`page-btn-${p + 1}`}
              >
                {p + 1}
              </button>
            {:else if p === page - 2 || p === page + 2}
              <span class="px-1 text-xs text-on-surface-variant font-bold" aria-hidden="true">...</span>
            {/if}
          {/each}
        </div>

        <button
          type="button"
          aria-label="Go to next page"
          disabled={isLast || page >= totalPages - 1 || isLoading}
          on:click={() => changePage(page + 1)}
          class="min-h-[36px] min-w-[36px] px-2.5 py-1.5 flex items-center justify-center rounded border border-outline-variant bg-surface-container-high text-on-surface hover:bg-surface-container-highest disabled:opacity-50 disabled:cursor-not-allowed focus:ring-2 focus:ring-primary focus:outline-none transition-colors"
          data-testid="next-page-btn"
        >
          <span class="sr-only sm:not-sr-only sm:mr-1 text-xs font-label-md">Next</span>
          <span class="material-symbols-outlined text-[18px]" aria-hidden="true">chevron_right</span>
        </button>
      </nav>
    </div>
  </footer>

  <!-- Authentication Modal -->
  <AuthModal
    bind:isOpen={isAuthModalOpen}
    on:close={closeAuthModal}
    on:success={handleAuthSuccess}
  />

  <!-- Create / Edit Material Modal Form -->
  {#if isFormOpen}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="form-modal-title"
      data-testid="catalog-form-modal"
    >
      <div class="bg-surface-container-lowest border border-outline-variant rounded-lg max-w-lg w-full p-6 shadow-xl flex flex-col gap-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between border-b border-outline-variant pb-3">
          <h2 id="form-modal-title" class="font-headline-sm text-headline-sm text-on-surface font-bold">
            {formData.id ? 'Edit Epidemiological Material' : 'Add Epidemiological Material'}
          </h2>
          <button
            type="button"
            aria-label="Close form"
            class="p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high focus:ring-2 focus:ring-primary focus:outline-none"
            on:click={closeForm}
            data-testid="close-form-btn"
          >
            <span class="material-symbols-outlined" aria-hidden="true">close</span>
          </button>
        </div>

        {#if formError}
          <div
            role="alert"
            class="p-3 bg-error-container border border-error text-on-error-container rounded font-body-sm text-xs flex items-start gap-2"
            data-testid="form-error-message"
          >
            <span class="material-symbols-outlined text-error shrink-0" style="font-size: 18px;" aria-hidden="true">error</span>
            <span>{formError}</span>
          </div>
        {/if}

        <form on:submit|preventDefault={handleFormSubmit} class="flex flex-col gap-4" data-testid="catalog-form">
          <div>
            <label for="material-title" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
              Title <span class="text-error">*</span>
            </label>
            <input
              id="material-title"
              type="text"
              required
              bind:value={formData.title}
              placeholder="e.g., Standard Protocol for Cholera Containment"
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="form-title-input"
            />
          </div>

          <div>
            <label for="material-category" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
              Category
            </label>
            <select
              id="material-category"
              bind:value={formData.category}
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="form-category-select"
            >
              <option value="protocol">Protocol</option>
              <option value="report">Report</option>
              <option value="dataset">Dataset</option>
              <option value="guideline">Guideline</option>
            </select>
          </div>

          <div>
            <label for="material-author" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
              Author / Organization
            </label>
            <input
              id="material-author"
              type="text"
              bind:value={formData.author}
              placeholder="e.g., CDC Epi Group"
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="form-author-input"
            />
          </div>

          <div>
            <label for="material-tags" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
              Tags (comma separated)
            </label>
            <input
              id="material-tags"
              type="text"
              bind:value={formData.tags}
              placeholder="e.g., cholera, waterborne, outbreak"
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="form-tags-input"
            />
          </div>

          <div>
            <label for="material-summary" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
              Summary / Description
            </label>
            <textarea
              id="material-summary"
              rows="3"
              bind:value={formData.summary}
              placeholder="Brief summary of the epidemiological material..."
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="form-summary-textarea"
            ></textarea>
          </div>

          <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
            <button
              type="button"
              class="px-4 py-2 border border-outline-variant rounded-DEFAULT hover:bg-surface-container-high font-label-md text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
              on:click={closeForm}
              data-testid="form-cancel-btn"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              class="px-4 py-2 bg-primary text-on-primary rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container font-label-md transition-colors focus:ring-2 focus:ring-primary focus:outline-none flex items-center gap-2"
              data-testid="form-submit-btn"
            >
              {#if isSubmitting}
                <span class="material-symbols-outlined animate-spin text-sm" aria-hidden="true">progress_activity</span>
              {/if}
              <span>{formData.id ? 'Save Changes' : 'Create Material'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  {/if}

  <!-- Delete Confirmation Dialog Modal -->
  {#if isDeleteModalOpen && itemToDelete}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-dialog-title"
      data-testid="delete-confirmation-dialog"
    >
      <div class="bg-surface-container-lowest border border-outline-variant rounded-lg max-w-md w-full p-6 shadow-xl flex flex-col gap-4">
        <div class="flex items-center gap-3 text-error">
          <span class="material-symbols-outlined text-2xl" aria-hidden="true">warning</span>
          <h2 id="delete-dialog-title" class="font-headline-sm text-headline-sm font-bold text-on-surface">
            Confirm Deletion
          </h2>
        </div>

        <p class="font-body-md text-on-surface-variant">
          Are you sure you want to delete <strong class="text-on-surface">"{itemToDelete.title}"</strong>?
          This action is irreversible and will remove the material from the catalog.
        </p>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
          <button
            type="button"
            class="px-4 py-2 border border-outline-variant rounded-DEFAULT hover:bg-surface-container-high font-label-md text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
            on:click={cancelDelete}
            data-testid="delete-cancel-btn"
          >
            Cancel
          </button>
          <button
            type="button"
            class="px-4 py-2 bg-error text-on-error rounded-DEFAULT hover:opacity-90 font-label-md transition-colors focus:ring-2 focus:ring-error focus:outline-none"
            on:click={confirmDelete}
            data-testid="delete-confirm-btn"
          >
            Delete Material
          </button>
        </div>
      </div>
    </div>
  {/if}
</div>
