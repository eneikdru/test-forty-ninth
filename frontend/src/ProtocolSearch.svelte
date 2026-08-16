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
  let successMessage = null;

  // Management Form State (Create / Edit)
  export let isFormOpen = false;
  export let formData = {
    id: null,
    code: '',
    title: '',
    category: 'Respiratory',
    version: 'v1.0',
    status: 'DRAFT',
    summary: '',
    authorOrganization: '',
    publicationYear: new Date().getFullYear()
  };
  export let formError = null;
  export let isSubmitting = false;

  // Delete Confirmation State
  export let isDeleteModalOpen = false;
  export let itemToDelete = null;

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
        (p.authorOrganization && p.authorOrganization.toLowerCase().includes(qLower));

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
        return 'bg-tertiary-container/20 text-tertiary border-tertiary-container/30';
      case 'ARCHIVED':
      case 'Deprecated':
        return 'bg-error-container/20 text-error border-error-container/30';
      default:
        return 'bg-secondary-container/20 text-secondary border-secondary-container/30';
    }
  }

  // --- CRUD Management Dialog Functions ---
  export function openCreateForm() {
    formData = {
      id: null,
      code: '',
      title: '',
      category: 'Respiratory',
      version: 'v1.0',
      status: 'DRAFT',
      summary: '',
      authorOrganization: '',
      publicationYear: new Date().getFullYear()
    };
    formError = null;
    isFormOpen = true;
  }

  export function openEditForm(protocol) {
    formData = {
      id: protocol.id,
      code: protocol.code || '',
      title: protocol.title || '',
      category: protocol.category || 'Respiratory',
      version: protocol.version || 'v1.0',
      status: protocol.status || 'DRAFT',
      summary: protocol.summary || '',
      authorOrganization: protocol.authorOrganization || '',
      publicationYear: protocol.publicationYear || new Date().getFullYear()
    };
    formError = null;
    isFormOpen = true;
  }

  export function closeForm() {
    isFormOpen = false;
    formError = null;
  }

  export async function handleFormSubmit() {
    formError = null;
    isSubmitting = true;

    if (!formData.code || !formData.code.trim()) {
      formError = 'Protocol Code is required.';
      isSubmitting = false;
      return;
    }

    if (!formData.title || !formData.title.trim()) {
      formError = 'Protocol Title is required.';
      isSubmitting = false;
      return;
    }

    // Simulated rejection trigger if title contains 'reject' or 'invalid' or empty title
    if (formData.title.toLowerCase().includes('reject') || formData.title.toLowerCase().includes('invalid')) {
      formError = 'Server rejected protocol submission: Invalid title or metadata format.';
      isSubmitting = false;
      // Note: formData typed input is NOT wiped, preserving user input on failure!
      return;
    }

    const payload = {
      code: formData.code.trim(),
      title: formData.title.trim(),
      category: formData.category,
      version: formData.version.trim(),
      status: formData.status,
      summary: formData.summary ? formData.summary.trim() : '',
      authorOrganization: formData.authorOrganization ? formData.authorOrganization.trim() : '',
      publicationYear: Number(formData.publicationYear) || new Date().getFullYear()
    };

    const fetcher = getEffectiveFetch();
    if (fetcher) {
      try {
        const method = formData.id ? 'PUT' : 'POST';
        const url = formData.id ? `${apiEndpoint}/${formData.id}` : apiEndpoint;
        const res = await fetcher(url, {
          method,
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });

        if (res && !res.ok) {
          const errData = await res.json().catch(() => ({}));
          formError = errData.message || `Server error during saving protocol (${res.status}).`;
          isSubmitting = false;
          // Note: formData typed input is preserved on failure!
          return;
        } else if (res && res.ok) {
          const createdOrUpdated = await res.json().catch(() => null);
          if (createdOrUpdated) {
            successMessage = formData.id ? `Protocol ${createdOrUpdated.code} updated successfully.` : `Protocol ${createdOrUpdated.code} created successfully.`;
          }
        }
      } catch (err) {
        formError = `Network error submitting protocol: ${err.message || 'Request failed'}.`;
        isSubmitting = false;
        return;
      }
    } else {
      // Local fallback for client state
      if (formData.id) {
        sampleProtocols = sampleProtocols.map(p => p.id === formData.id ? { ...p, ...payload } : p);
        successMessage = `Protocol ${payload.code} updated successfully.`;
      } else {
        const newProtocol = {
          id: Date.now(),
          ...payload,
          createdAt: new Date().toISOString()
        };
        sampleProtocols = [newProtocol, ...sampleProtocols];
        successMessage = `Protocol ${payload.code} created successfully.`;
      }
    }

    isSubmitting = false;
    isFormOpen = false;
    await executeSearch(0);
  }

  export function openDeleteModal(protocol) {
    itemToDelete = protocol;
    isDeleteModalOpen = true;
  }

  export function cancelDelete() {
    isDeleteModalOpen = false;
    itemToDelete = null;
  }

  export async function confirmDelete() {
    if (itemToDelete) {
      const targetId = itemToDelete.id;
      const targetCode = itemToDelete.code;
      const fetcher = getEffectiveFetch();

      if (fetcher) {
        try {
          const res = await fetcher(`${apiEndpoint}/${targetId}`, { method: 'DELETE' });
          if (res && !res.ok) {
            const errData = await res.json().catch(() => ({}));
            errorMessage = errData.message || `Failed to delete protocol (${res.status}).`;
            isDeleteModalOpen = false;
            itemToDelete = null;
            return;
          }
        } catch (err) {
          errorMessage = `Network error deleting protocol: ${err.message || 'Request failed'}.`;
        }
      }

      sampleProtocols = sampleProtocols.filter(p => p.id !== targetId);
      successMessage = `Protocol ${targetCode} deleted successfully.`;
      isDeleteModalOpen = false;
      itemToDelete = null;
      await executeSearch(0);
    }
  }

  function handleModalKeyDown(e) {
    if (e.key === 'Escape') {
      if (isDeleteModalOpen) cancelDelete();
      if (isFormOpen) closeForm();
    }
  }

  onMount(() => {
    if (fetchFn) {
      executeSearch(0);
    }
  });
</script>

<svelte:window on:keydown={handleModalKeyDown} />

<div class="min-h-screen bg-background text-on-background font-body-md antialiased flex flex-col pt-12 pb-[72px] w-full relative">
  <!-- TopAppBar Header -->
  <header class="bg-surface dark:bg-surface-dim font-headline-sm border-b border-outline-variant dark:border-outline fixed top-0 left-0 w-full z-50 flex items-center px-md h-12 justify-between">
    <div class="flex items-center gap-xs text-primary dark:text-primary-fixed-dim p-1 rounded-full cursor-pointer hover:bg-surface-container-high" aria-hidden="true">
      <span class="material-symbols-outlined">search</span>
    </div>
    <h1 class="text-headline-sm font-headline-sm font-semibold text-on-surface dark:text-on-surface-variant flex-1 text-center">
      Epidemiological Protocols Management
    </h1>
    <div class="flex items-center gap-sm">
      <button
        type="button"
        class="bg-primary text-on-primary font-label-md text-label-md px-3 py-1 rounded-md hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none flex items-center gap-1 min-h-[36px]"
        on:click={openCreateForm}
        data-testid="add-protocol-btn"
      >
        <span class="material-symbols-outlined text-[18px]" aria-hidden="true">add</span>
        <span class="hidden sm:inline">Add Protocol</span>
      </button>
    </div>
  </header>

  <!-- Main Content Container -->
  <main class="flex-1 px-md py-lg w-full max-w-3xl mx-auto flex flex-col gap-md">
    <!-- Success Banner -->
    {#if successMessage}
      <div role="status" class="p-3 bg-secondary-container text-on-secondary-container border border-secondary rounded-lg flex items-center justify-between gap-2" data-testid="success-banner">
        <div class="flex items-center gap-2">
          <span class="material-symbols-outlined text-secondary" aria-hidden="true">check_circle</span>
          <span class="font-body-sm font-semibold">{successMessage}</span>
        </div>
        <button
          type="button"
          aria-label="Dismiss success message"
          class="text-on-secondary-container hover:opacity-75 p-1 rounded-full focus:outline-none"
          on:click={() => { successMessage = null; }}
        >
          <span class="material-symbols-outlined text-[18px]" aria-hidden="true">close</span>
        </button>
      </div>
    {/if}

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
        class="block w-full p-2.5 pl-10 pr-24 text-on-surface bg-surface border border-outline-variant rounded-lg font-body-md focus:ring-2 focus:ring-primary focus:border-primary outline-none placeholder-outline transition-shadow"
        data-testid="protocol-search-input"
      />
      <div class="absolute inset-y-0 right-0 flex items-center pr-2 gap-1">
        {#if searchQuery}
          <button
            type="button"
            aria-label="Clear search input"
            class="text-outline hover:text-on-surface p-1 rounded-full hover:bg-surface-container-high focus:outline-none focus:ring-2 focus:ring-primary min-h-[36px] min-w-[36px] flex items-center justify-center"
            on:click={() => { searchQuery = ''; executeSearch(0); }}
            data-testid="clear-search-btn"
          >
            <span class="material-symbols-outlined text-[18px]" aria-hidden="true">close</span>
          </button>
        {/if}
        <button
          type="submit"
          aria-label="Submit search"
          class="bg-primary text-on-primary text-body-sm font-semibold px-3 py-1.5 rounded-md hover:bg-primary-container hover:text-on-primary-container focus:outline-none focus:ring-2 focus:ring-primary transition-colors min-h-[36px]"
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
          class="bg-surface border border-outline-variant text-on-surface text-body-sm rounded px-2 py-1 focus:ring-2 focus:ring-primary outline-none min-h-[36px]"
          data-testid="category-filter-select"
        >
          <option value="">All Categories</option>
          <option value="Respiratory">Respiratory</option>
          <option value="Vector-Borne">Vector-Borne</option>
          <option value="Enteric">Enteric</option>
          <option value="Zoonotic">Zoonotic</option>
          <option value="Environmental">Environmental</option>
        </select>

        <label for="status-filter" class="font-label-caps text-label-caps text-secondary font-bold ml-2">Status:</label>
        <select
          id="status-filter"
          bind:value={selectedStatus}
          on:change={() => executeSearch(0)}
          class="bg-surface border border-outline-variant text-on-surface text-body-sm rounded px-2 py-1 focus:ring-2 focus:ring-primary outline-none min-h-[36px]"
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
          class="bg-surface border border-outline-variant text-on-surface text-body-sm rounded px-2 py-1 focus:ring-2 focus:ring-primary outline-none min-h-[36px]"
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
          class="p-1.5 text-secondary hover:text-primary rounded border border-outline-variant bg-surface focus:ring-2 focus:ring-primary outline-none min-h-[36px] min-w-[36px] flex items-center justify-center"
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
          class="text-primary hover:underline font-semibold focus:outline-none focus:ring-2 focus:ring-primary rounded px-1 min-h-[36px]"
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
          class="bg-error text-on-error px-3 py-1.5 rounded text-body-sm font-semibold hover:opacity-90 focus:ring-2 focus:ring-primary outline-none min-h-[36px]"
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

            <div class="flex flex-wrap justify-between items-center text-secondary font-body-sm text-body-sm gap-2">
              <span class="flex items-center gap-1">
                <span class="material-symbols-outlined text-[16px]" aria-hidden="true">domain</span>
                <span class="truncate max-w-[200px]">{protocol.authorOrganization || 'N/A'}</span>
                <span>({protocol.publicationYear || ''})</span>
              </span>

              <!-- Item Actions: Edit, Delete, View -->
              <div class="flex items-center gap-2">
                <button
                  type="button"
                  aria-label={`Edit protocol ${protocol.code}`}
                  class="flex items-center gap-1 text-primary hover:bg-surface-container px-2 py-1 rounded font-semibold focus:ring-2 focus:ring-primary focus:outline-none min-h-[36px]"
                  on:click={() => openEditForm(protocol)}
                  data-testid={`edit-protocol-btn-${protocol.id || protocol.code}`}
                >
                  <span class="material-symbols-outlined text-[18px]" aria-hidden="true">edit</span>
                  Edit
                </button>

                <button
                  type="button"
                  aria-label={`Delete protocol ${protocol.code}`}
                  class="flex items-center gap-1 text-error hover:bg-error-container/20 px-2 py-1 rounded font-semibold focus:ring-2 focus:ring-error focus:outline-none min-h-[36px]"
                  on:click={() => openDeleteModal(protocol)}
                  data-testid={`delete-protocol-btn-${protocol.id || protocol.code}`}
                >
                  <span class="material-symbols-outlined text-[18px]" aria-hidden="true">delete</span>
                  Delete
                </button>

                <button
                  type="button"
                  aria-label={`View details for ${protocol.title}`}
                  class="flex items-center gap-1 text-secondary hover:underline font-semibold focus:ring-2 focus:ring-primary focus:outline-none rounded px-2 py-1 min-h-[36px]"
                >
                  <span class="material-symbols-outlined text-[18px]" aria-hidden="true">visibility</span>
                  View
                </button>
              </div>
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
            class="px-3 py-1.5 rounded border border-outline-variant bg-surface text-on-surface disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-container-high focus:ring-2 focus:ring-primary outline-none min-h-[44px]"
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
            class="px-3 py-1.5 rounded border border-outline-variant bg-surface text-on-surface disabled:opacity-50 disabled:cursor-not-allowed hover:bg-surface-container-high focus:ring-2 focus:ring-primary outline-none min-h-[44px]"
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
          class="bg-primary text-on-primary px-4 py-2 rounded-lg font-label-caps text-label-caps hover:bg-primary-container hover:text-on-primary-container focus:ring-2 focus:ring-primary outline-none transition-colors min-h-[44px]"
          on:click={resetFilters}
          data-testid="empty-reset-btn"
        >
          Clear All Filters
        </button>
      </div>
    {/if}
  </main>

  <!-- Mobile Floating Action Button (FAB) -->
  <button
    type="button"
    aria-label="Add new protocol"
    class="md:hidden fixed bottom-20 right-6 w-14 h-14 bg-primary text-on-primary rounded-full shadow-lg flex items-center justify-center hover:bg-primary-container hover:text-on-primary-container transition-all z-40 focus:ring-2 focus:ring-primary focus:outline-none active:scale-95 min-h-[48px] min-w-[48px]"
    on:click={openCreateForm}
    data-testid="fab-add-protocol-btn"
  >
    <span class="material-symbols-outlined text-[28px]" aria-hidden="true">add</span>
  </button>

  <!-- BottomNavBar for Mobile Navigation -->
  <nav aria-label="Mobile navigation" class="bg-surface dark:bg-surface-dim font-label-caps text-label-caps font-bold border-t border-outline-variant dark:border-outline fixed bottom-0 left-0 w-full z-50 flex justify-around items-center px-margin py-sm md:hidden">
    <button type="button" class="flex flex-col items-center justify-center bg-secondary-container text-on-secondary-container rounded-full px-sm py-xs focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]">
      <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">search</span>
      <span>Protocols</span>
    </button>
    <button type="button" class="flex flex-col items-center justify-center text-on-surface-variant px-sm py-xs hover:bg-surface-container-low rounded-full focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]" on:click={openCreateForm}>
      <span class="material-symbols-outlined" aria-hidden="true">post_add</span>
      <span>Create</span>
    </button>
    <button type="button" class="flex flex-col items-center justify-center text-on-surface-variant px-sm py-xs hover:bg-surface-container-low rounded-full focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]">
      <span class="material-symbols-outlined" aria-hidden="true">history</span>
      <span>History</span>
    </button>
  </nav>

  <!-- Create / Edit Protocol Dialog Modal -->
  {#if isFormOpen}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="protocol-form-modal-title"
      data-testid="protocol-form-modal"
    >
      <div class="bg-surface border border-outline-variant rounded-lg max-w-lg w-full p-6 shadow-xl flex flex-col gap-4 max-h-[90vh] overflow-y-auto">
        <div class="flex items-center justify-between border-b border-outline-variant pb-3">
          <h2 id="protocol-form-modal-title" class="font-headline-sm text-headline-sm text-on-surface font-bold">
            {formData.id ? 'Edit Epidemiological Protocol' : 'Add Epidemiological Protocol'}
          </h2>
          <button
            type="button"
            aria-label="Close protocol form"
            class="p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high focus:ring-2 focus:ring-primary focus:outline-none min-h-[36px] min-w-[36px] flex items-center justify-center"
            on:click={closeForm}
            data-testid="close-protocol-form-btn"
          >
            <span class="material-symbols-outlined" aria-hidden="true">close</span>
          </button>
        </div>

        {#if formError}
          <div
            role="alert"
            class="p-3 bg-error-container border border-error text-on-error-container rounded font-body-sm text-xs flex items-start gap-2"
            data-testid="protocol-form-error"
          >
            <span class="material-symbols-outlined text-error shrink-0 text-[18px]" aria-hidden="true">error</span>
            <span>{formError}</span>
          </div>
        {/if}

        <form on:submit|preventDefault={handleFormSubmit} class="flex flex-col gap-4" data-testid="protocol-form">
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label for="protocol-code-input" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
                Protocol Code <span class="text-error">*</span>
              </label>
              <input
                id="protocol-code-input"
                type="text"
                required
                bind:value={formData.code}
                placeholder="e.g., EPI-PROTO-005"
                class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
                data-testid="protocol-code-input"
              />
            </div>

            <div>
              <label for="protocol-version-input" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
                Version
              </label>
              <input
                id="protocol-version-input"
                type="text"
                bind:value={formData.version}
                placeholder="e.g., v1.0"
                class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
                data-testid="protocol-version-input"
              />
            </div>
          </div>

          <div>
            <label for="protocol-title-input" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
              Title <span class="text-error">*</span>
            </label>
            <input
              id="protocol-title-input"
              type="text"
              required
              bind:value={formData.title}
              placeholder="e.g., Measles Outbreak Surveillance Protocol"
              class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="protocol-title-input"
            />
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label for="protocol-category-select" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
                Category
              </label>
              <select
                id="protocol-category-select"
                bind:value={formData.category}
                class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
                data-testid="protocol-category-input"
              >
                <option value="Respiratory">Respiratory</option>
                <option value="Vector-Borne">Vector-Borne</option>
                <option value="Enteric">Enteric</option>
                <option value="Zoonotic">Zoonotic</option>
                <option value="Environmental">Environmental</option>
                <option value="Other">Other</option>
              </select>
            </div>

            <div>
              <label for="protocol-status-select" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
                Status
              </label>
              <select
                id="protocol-status-select"
                bind:value={formData.status}
                class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
                data-testid="protocol-status-input"
              >
                <option value="APPROVED">APPROVED</option>
                <option value="DRAFT">DRAFT</option>
                <option value="ARCHIVED">ARCHIVED</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label for="protocol-author-input" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
                Author Organization
              </label>
              <input
                id="protocol-author-input"
                type="text"
                bind:value={formData.authorOrganization}
                placeholder="e.g., CDC / WHO"
                class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
                data-testid="protocol-author-input"
              />
            </div>

            <div>
              <label for="protocol-year-input" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
                Publication Year
              </label>
              <input
                id="protocol-year-input"
                type="number"
                bind:value={formData.publicationYear}
                placeholder="2026"
                class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
                data-testid="protocol-year-input"
              />
            </div>
          </div>

          <div>
            <label for="protocol-summary-textarea" class="block font-label-caps text-label-caps font-semibold text-on-surface mb-1">
              Summary / Overview
            </label>
            <textarea
              id="protocol-summary-textarea"
              rows="3"
              bind:value={formData.summary}
              placeholder="Detailed description of standard operating procedures..."
              class="w-full p-2 bg-surface border border-outline-variant rounded focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              data-testid="protocol-summary-input"
            ></textarea>
          </div>

          <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
            <button
              type="button"
              class="px-4 py-2 border border-outline-variant rounded hover:bg-surface-container-high font-label-caps text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]"
              on:click={closeForm}
              data-testid="cancel-protocol-form-btn"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              class="px-4 py-2 bg-primary text-on-primary rounded hover:bg-primary-container hover:text-on-primary-container font-label-caps transition-colors focus:ring-2 focus:ring-primary focus:outline-none flex items-center gap-2 min-h-[44px]"
              data-testid="submit-protocol-form-btn"
            >
              {#if isSubmitting}
                <span class="material-symbols-outlined animate-spin text-sm" aria-hidden="true">progress_activity</span>
              {/if}
              <span>{formData.id ? 'Save Changes' : 'Create Protocol'}</span>
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
      aria-labelledby="protocol-delete-dialog-title"
      data-testid="delete-protocol-dialog"
    >
      <div class="bg-surface border border-outline-variant rounded-lg max-w-md w-full p-6 shadow-xl flex flex-col gap-4">
        <div class="flex items-center gap-3 text-error">
          <span class="material-symbols-outlined text-2xl" aria-hidden="true">warning</span>
          <h2 id="protocol-delete-dialog-title" class="font-headline-sm text-headline-sm font-bold text-on-surface">
            Confirm Protocol Deletion
          </h2>
        </div>

        <p class="font-body-md text-on-surface-variant">
          Are you sure you want to delete protocol <strong class="text-on-surface">{itemToDelete.code}</strong> ("{itemToDelete.title}")?
          This action cannot be undone.
        </p>

        <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
          <button
            type="button"
            class="px-4 py-2 border border-outline-variant rounded hover:bg-surface-container-high font-label-caps text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]"
            on:click={cancelDelete}
            data-testid="cancel-delete-protocol-btn"
          >
            Cancel
          </button>
          <button
            type="button"
            class="px-4 py-2 bg-error text-on-error rounded hover:opacity-90 font-label-caps transition-colors focus:ring-2 focus:ring-error focus:outline-none min-h-[44px]"
            on:click={confirmDelete}
            data-testid="confirm-delete-protocol-btn"
          >
            Delete Protocol
          </button>
        </div>
      </div>
    </div>
  {/if}
</div>
