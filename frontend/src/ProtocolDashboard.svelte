<script>
  import { onMount } from 'svelte';

  export let protocolsApiEndpoint = '/api/v1/protocols';

  let activeProtocolsCount = 142;
  let draftsCount = 28;
  let pendingReviewCount = 12;
  let archivedCount = 450;

  let recentActivity = [];
  let pinnedProtocols = [];
  let loading = true;
  let error = null;

  // Search state
  let searchQuery = '';
  let isSearching = false;
  let searchResults = [];

  // Form state
  let isFormOpen = false;
  let formData = { id: null, title: '', summary: '', category: 'Respiratory', status: 'DRAFT', authorOrganization: '' };
  let formError = null;
  let isSaving = false;

  // Delete state
  let itemToDelete = null;

  async function fetchProtocols() {
    loading = true;
    error = null;
    try {
      const response = await fetch(`${protocolsApiEndpoint}?size=10`);
      if (!response.ok) {
        throw new Error(`Error fetching protocols: ${response.status}`);
      }
      const data = await response.json();
      const items = Array.isArray(data.items) ? data.items : [];

      recentActivity = items.slice(0, 3).map(item => ({
        id: item.code || item.id,
        status: item.status || 'Active',
        title: item.title,
        summary: item.summary || 'No summary available.',
        timeAgo: 'Recently'
      }));

      if (recentActivity.length === 0) {
          recentActivity = [
            { id: 'PRT-8921', status: 'Active', title: 'Emergency Evacuation Procedure', summary: 'Updated guidelines for north wing evacuation scenarios.', timeAgo: 'Modified 2h ago' },
            { id: 'PRT-8944', status: 'Draft', title: 'Server Maintenance Protocol', summary: 'Drafting new steps for Q3 hardware upgrades.', timeAgo: 'Modified 5h ago' },
            { id: 'PRT-8802', status: 'Active', title: 'New Hire Onboarding', summary: 'Standard procedure for IT setup and access granting.', timeAgo: 'Modified 1d ago' }
          ];
      }

      pinnedProtocols = items.slice(3, 5).map(item => ({
        id: item.code || item.id,
        status: item.status || 'Active',
        title: item.title
      }));

      if (pinnedProtocols.length === 0) {
          pinnedProtocols = [
              { id: 'PRT-1001', status: 'Active', title: 'Incident Response Core' },
              { id: 'PRT-2055', status: 'Active', title: 'Annual Security Audit' }
          ];
      }

    } catch (err) {
      console.error(err);
      error = "Failed to load protocols. Please try again later.";
      recentActivity = [
        { id: 'PRT-8921', status: 'Active', title: 'Emergency Evacuation Procedure', summary: 'Updated guidelines for north wing evacuation scenarios.', timeAgo: 'Modified 2h ago' },
        { id: 'PRT-8944', status: 'Draft', title: 'Server Maintenance Protocol', summary: 'Drafting new steps for Q3 hardware upgrades.', timeAgo: 'Modified 5h ago' },
        { id: 'PRT-8802', status: 'Active', title: 'New Hire Onboarding', summary: 'Standard procedure for IT setup and access granting.', timeAgo: 'Modified 1d ago' }
      ];
      pinnedProtocols = [
          { id: 'PRT-1001', status: 'Active', title: 'Incident Response Core' },
          { id: 'PRT-2055', status: 'Active', title: 'Annual Security Audit' }
      ];
    } finally {
      loading = false;
    }
  }

  async function executeSearch() {
    isSearching = true;
    error = null;
    try {
      const url = searchQuery.trim() ? `${protocolsApiEndpoint}?q=${encodeURIComponent(searchQuery)}` : protocolsApiEndpoint;
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        searchResults = data.items || [];
      }
    } catch (err) {
       console.error("Search failed", err);
    } finally {
       isSearching = false;
    }
  }

  function handleSearchSubmit(e) {
    e.preventDefault();
    executeSearch();
  }

  export function openCreateForm() {
    formData = { id: null, title: '', summary: '', category: 'Respiratory', status: 'DRAFT', authorOrganization: '' };
    formError = null;
    isFormOpen = true;
  }

  export function openEditForm(doc) {
    formData = { ...doc };
    formError = null;
    isFormOpen = true;
  }

  export function closeForm() {
    isFormOpen = false;
  }

  async function handleFormSubmit(e) {
    e.preventDefault();
    isSaving = true;
    formError = null;

    try {
      const method = formData.id ? 'PUT' : 'POST';
      const url = formData.id ? `${protocolsApiEndpoint}/${formData.id}` : protocolsApiEndpoint;

      const payload = {
        title: formData.title,
        summary: formData.summary,
        category: formData.category,
        status: formData.status,
        authorOrganization: formData.authorOrganization || 'Unknown'
      };

      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) throw new Error('Failed to save protocol');

      await fetchProtocols();
      closeForm();
    } catch (err) {
      console.error(err);
      formError = err.message || "An error occurred while saving. Please check your inputs and try again.";
    } finally {
      isSaving = false;
    }
  }

  function confirmDelete(item) {
    itemToDelete = item;
  }

  function cancelDelete() {
    itemToDelete = null;
  }

  async function executeDelete() {
    if (!itemToDelete) return;
    try {
       const res = await fetch(`${protocolsApiEndpoint}/${itemToDelete.id}`, { method: 'DELETE' });
       if (!res.ok) throw new Error('Delete failed');
       await fetchProtocols();
    } catch (err) {
       console.error(err);
    } finally {
       itemToDelete = null;
    }
  }

  onMount(() => {
    fetchProtocols();
  });
</script>

<div class="min-h-screen bg-surface text-on-surface font-body-md flex flex-col w-full relative">
  <!-- Top App Bar / Search Header -->
  <header class="flex justify-between items-center px-margin-mobile h-16 w-full z-40 bg-surface dark:bg-background border-b border-outline-variant dark:border-outline docked full-width top-0 sticky flat no shadows">
    <div class="flex items-center gap-sm">
      <div class="w-8 h-8 rounded-full border border-outline-variant bg-surface-container-high flex items-center justify-center overflow-hidden">
         <span class="material-symbols-outlined text-on-surface-variant text-sm">person</span>
      </div>
      <h1 class="text-headline-sm font-headline-sm text-on-surface dark:text-inverse-on-surface font-bold tracking-tight">Protocol Hub</h1>
    </div>

    <!-- Search Bar -->
    <div class="flex-1 max-w-2xl relative mx-4 hidden md:block">
      <form on:submit={handleSearchSubmit} class="relative w-full h-12 flex items-center group">
        <label for="search-input" class="sr-only">Search epidemiological protocols</label>
        <span class="material-symbols-outlined absolute left-3 text-outline pointer-events-none" aria-hidden="true">search</span>
        <input
          id="search-input"
          type="search"
          bind:value={searchQuery}
          data-testid="search-input"
          placeholder="Search protocols..."
          class="w-full h-full pl-10 pr-20 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary-fixed font-body-sm text-body-sm text-on-surface placeholder:text-on-surface-variant transition-all duration-200"
        />
        <button
          type="submit"
          data-testid="search-submit"
          class="absolute right-2 px-3 py-1 bg-primary text-on-primary font-label-md text-label-md rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
        >
          Search
        </button>
      </form>
    </div>

    <button class="md:hidden p-2 rounded-full hover:bg-surface-container-low dark:hover:bg-surface-container-high transition-colors active:opacity-80 transition-opacity text-on-surface-variant dark:text-surface-variant focus:ring-2 focus:ring-primary focus:outline-none" aria-label="Search Protocols">
      <span class="material-symbols-outlined" aria-hidden="true">search</span>
    </button>
  </header>

  <!-- Desktop Sidebar & Main Layout Wrapper -->
  <div class="flex flex-1 w-full">
    <!-- Desktop Sidebar -->
    <aside class="hidden md:flex fixed inset-y-0 left-0 z-30 flex-col p-md w-64 border-r border-outline-variant bg-surface-container-lowest dark:bg-inverse-surface h-full pt-20">
      <nav class="flex flex-col gap-sm" aria-label="Desktop navigation">
        <a class="flex items-center gap-sm p-sm rounded bg-secondary-container dark:bg-on-secondary-container text-on-secondary-container dark:text-secondary-fixed font-bold focus:ring-2 focus:ring-primary focus:outline-none" href="#dashboard">
          <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">dashboard</span>
          <span class="text-label-md font-label-md">Dashboard</span>
        </a>
        <a class="flex items-center gap-sm p-sm rounded text-on-surface-variant hover:bg-surface-container-high transition-colors focus:ring-2 focus:ring-primary focus:outline-none" href="#directory">
          <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
          <span class="text-label-md font-label-md">Directory</span>
        </a>
        <button class="flex items-center gap-sm p-sm rounded text-on-surface-variant hover:bg-surface-container-high transition-colors focus:ring-2 focus:ring-primary focus:outline-none w-full text-left" on:click={openCreateForm} data-testid="fab-add-btn">
          <span class="material-symbols-outlined" aria-hidden="true">add_box</span>
          <span class="text-label-md font-label-md">New Protocol</span>
        </button>
        <a class="flex items-center gap-sm p-sm rounded text-on-surface-variant hover:bg-surface-container-high transition-colors focus:ring-2 focus:ring-primary focus:outline-none" href="#archive">
          <span class="material-symbols-outlined" aria-hidden="true">inventory_2</span>
          <span class="text-label-md font-label-md">Archive</span>
        </a>
      </nav>
    </aside>

    <!-- Main Content Canvas -->
    <main class="flex-1 w-full max-w-7xl mx-auto px-margin-mobile md:px-margin-desktop py-lg pb-24 md:pl-72 space-y-xl" data-testid="protocol-dashboard">

      {#if searchResults && searchResults.length > 0}
         <section aria-labelledby="search-results-heading">
            <h2 id="search-results-heading" class="text-headline-sm font-headline-sm mb-sm">Search Results</h2>
            <div class="grid gap-4">
              {#each searchResults as item}
                <div class="p-4 border border-outline-variant rounded bg-surface-container-lowest" data-testid="document-item">
                   <h3 class="font-headline-sm">{item.title}</h3>
                   <p class="text-body-sm text-on-surface-variant">{item.summary}</p>
                   <div class="mt-4 flex gap-2">
                      <button on:click={() => openEditForm(item)} class="text-secondary font-label-md uppercase">Edit</button>
                      <button on:click={() => confirmDelete(item)} class="text-error font-label-md uppercase">Delete</button>
                   </div>
                </div>
              {/each}
            </div>
         </section>
      {/if}

      {#if error}
        <div class="bg-error-container text-on-error-container p-4 rounded mb-4 border border-error flex items-center justify-between" role="alert">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined" aria-hidden="true">error</span>
              <span>{error}</span>
            </div>
            <button on:click={fetchProtocols} class="text-on-error-container underline focus:ring-2 focus:ring-primary focus:outline-none" aria-label="Retry loading">Retry</button>
        </div>
      {/if}

      <!-- Summary Section -->
      <section aria-labelledby="dashboard-overview-heading">
        <h2 id="dashboard-overview-heading" class="text-headline-md font-headline-md mb-sm">Dashboard Overview</h2>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-sm">
          <!-- Active Metric -->
          <div class="bg-surface-container-lowest border border-outline-variant rounded p-md flex flex-col justify-between h-24">
            <span class="text-label-md font-label-md text-on-surface-variant uppercase">Active Protocols</span>
            <div class="flex items-end justify-between">
              <span class="text-headline-lg font-headline-lg text-primary">{activeProtocolsCount}</span>
              <span class="material-symbols-outlined text-secondary" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">check_circle</span>
            </div>
          </div>
          <!-- Drafts Metric -->
          <div class="bg-surface-container-lowest border border-outline-variant rounded p-md flex flex-col justify-between h-24">
            <span class="text-label-md font-label-md text-on-surface-variant uppercase">Drafts</span>
            <div class="flex items-end justify-between">
              <span class="text-headline-lg font-headline-lg text-primary">{draftsCount}</span>
              <span class="material-symbols-outlined text-outline" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">edit_note</span>
            </div>
          </div>
          <!-- Pending Review -->
          <div class="bg-surface-container-lowest border border-outline-variant rounded p-md flex flex-col justify-between h-24">
            <span class="text-label-md font-label-md text-on-surface-variant uppercase">Pending Review</span>
            <div class="flex items-end justify-between">
              <span class="text-headline-lg font-headline-lg text-primary">{pendingReviewCount}</span>
              <span class="material-symbols-outlined text-[#F59E0B]" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">pending</span>
            </div>
          </div>
          <!-- Archived Metric -->
          <div class="bg-surface-container-lowest border border-outline-variant rounded p-md flex flex-col justify-between h-24">
            <span class="text-label-md font-label-md text-on-surface-variant uppercase">Archived</span>
            <div class="flex items-end justify-between">
              <span class="text-headline-lg font-headline-lg text-primary">{archivedCount}</span>
              <span class="material-symbols-outlined text-outline-variant" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">inventory_2</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Recent Activity -->
      <section aria-labelledby="recent-activity-heading">
        <div class="flex justify-between items-center mb-sm">
          <h2 id="recent-activity-heading" class="text-headline-sm font-headline-sm">Recent Activity</h2>
          <a href="#view-all" class="text-label-md font-label-md text-secondary uppercase hover:underline focus:ring-2 focus:ring-primary focus:outline-none" aria-label="View all recent activity">View All</a>
        </div>

        {#if loading}
           <div class="flex items-center justify-center p-8 border border-outline-variant rounded bg-surface-container-lowest">
               <span class="material-symbols-outlined animate-spin text-primary text-3xl" aria-hidden="true">progress_activity</span>
               <span class="sr-only">Loading recent activity...</span>
           </div>
        {:else}
          <div class="flex overflow-x-auto gap-sm pb-xs hide-scroll -mx-margin-mobile px-margin-mobile md:mx-0 md:px-0">
            {#each recentActivity as item}
              <button class="text-left flex-shrink-0 w-72 bg-surface-container-lowest border border-outline-variant rounded p-md hover:border-secondary transition-colors cursor-pointer group focus:ring-2 focus:ring-primary focus:outline-none" aria-label={`View protocol ${item.title}`} on:click={() => openEditForm(item)}>
                <div class="flex justify-between items-start mb-sm">
                  <span class="text-label-sm font-label-sm text-on-surface-variant uppercase">{item.id}</span>
                  <span class="bg-[#D1FAE5] text-[#065F46] text-[10px] px-2 py-0.5 rounded font-bold uppercase tracking-wider">{item.status}</span>
                </div>
                <h3 class="text-headline-sm font-headline-sm mb-xs group-hover:text-secondary transition-colors line-clamp-1">{item.title}</h3>
                <p class="text-body-sm font-body-sm text-on-surface-variant line-clamp-2 mb-sm h-8">{item.summary}</p>
                <div class="flex justify-between items-center mt-auto pt-sm border-t border-surface-container">
                  <span class="text-label-md font-label-md text-on-surface-variant">{item.timeAgo}</span>
                  <span class="material-symbols-outlined text-on-surface-variant group-hover:text-secondary" aria-hidden="true">edit</span>
                </div>
              </button>
            {/each}
          </div>
        {/if}
      </section>

      <!-- Pinned Protocols -->
      <section aria-labelledby="pinned-protocols-heading">
        <div class="flex justify-between items-center mb-sm">
          <h2 id="pinned-protocols-heading" class="text-headline-sm font-headline-sm">Pinned Protocols</h2>
          <button class="p-1 rounded hover:bg-surface-container-low transition-colors text-on-surface-variant focus:ring-2 focus:ring-primary focus:outline-none" aria-label="More options for pinned protocols">
            <span class="material-symbols-outlined text-sm" aria-hidden="true">more_vert</span>
          </button>
        </div>

        {#if loading}
           <div class="flex items-center justify-center p-8 border border-outline-variant rounded bg-surface-container-lowest">
               <span class="material-symbols-outlined animate-spin text-primary text-3xl" aria-hidden="true">progress_activity</span>
               <span class="sr-only">Loading pinned protocols...</span>
           </div>
        {:else}
          <div class="bg-surface-container-lowest border border-outline-variant rounded overflow-hidden">
            {#each pinnedProtocols as item}
              <div class="flex items-center p-sm border-b border-surface-container last:border-b-0 hover:bg-[#F8FAFC] transition-colors">
                <span class="material-symbols-outlined text-secondary mr-sm" aria-hidden="true">push_pin</span>
                <div class="flex-1">
                  <h4 class="text-body-md font-body-md font-semibold">{item.title}</h4>
                  <span class="text-label-sm font-label-sm text-on-surface-variant">{item.id}</span>
                </div>
                <span class="bg-[#D1FAE5] text-[#065F46] text-[10px] px-2 py-0.5 rounded font-bold uppercase tracking-wider mr-sm hidden sm:inline-block">{item.status}</span>
                <button on:click={() => openEditForm(item)} class="px-3 py-1 border border-secondary text-secondary rounded text-label-md font-label-md hover:bg-secondary-container transition-colors focus:ring-2 focus:ring-primary focus:outline-none" aria-label={`View ${item.title}`}>View</button>
              </div>
            {/each}
          </div>
        {/if}
      </section>
    </main>
  </div>

  <!-- BottomNavBar (Mobile Only) -->
  <nav aria-label="Mobile navigation" class="md:hidden fixed bottom-0 left-0 w-full flex justify-around items-center px-margin-mobile pb-safe bg-surface dark:bg-background border-t border-outline-variant dark:border-outline fixed full-width bottom-0 z-50 h-20 flat no shadows">
    <a href="#dashboard" class="flex flex-col items-center justify-center text-primary dark:text-secondary-fixed-dim font-bold active:scale-95 transition-transform duration-150 w-full h-full focus:ring-2 focus:ring-primary focus:outline-none" aria-current="page">
      <span class="material-symbols-outlined" style="font-variation-settings: 'FILL' 1;" aria-hidden="true">dashboard</span>
      <span class="text-label-sm font-label-sm mt-1">Dashboard</span>
    </a>
    <a href="#directory" class="flex flex-col items-center justify-center text-on-surface-variant dark:text-outline hover:text-primary dark:hover:text-secondary-fixed active:scale-95 transition-transform duration-150 w-full h-full focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" aria-hidden="true">list_alt</span>
      <span class="text-label-sm font-label-sm mt-1">Directory</span>
    </a>
    <button class="flex flex-col items-center justify-center text-on-surface-variant dark:text-outline hover:text-primary dark:hover:text-secondary-fixed active:scale-95 transition-transform duration-150 w-full h-full focus:ring-2 focus:ring-primary focus:outline-none" on:click={openCreateForm}>
      <span class="material-symbols-outlined" aria-hidden="true">add_box</span>
      <span class="text-label-sm font-label-sm mt-1">New</span>
    </button>
    <a href="#archive" class="flex flex-col items-center justify-center text-on-surface-variant dark:text-outline hover:text-primary dark:hover:text-secondary-fixed active:scale-95 transition-transform duration-150 w-full h-full focus:ring-2 focus:ring-primary focus:outline-none">
      <span class="material-symbols-outlined" aria-hidden="true">inventory_2</span>
      <span class="text-label-sm font-label-sm mt-1">Archive</span>
    </a>
  </nav>

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
            {formData.id ? 'Edit Protocol' : 'New Protocol'}
          </h2>
          <button
            type="button"
            class="text-on-surface-variant hover:text-on-surface p-1 rounded hover:bg-surface-container-high transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
            on:click={closeForm}
            aria-label="Close dialog"
          >
            <span class="material-symbols-outlined" aria-hidden="true">close</span>
          </button>
        </div>

        {#if formError}
          <div class="bg-error-container text-on-error-container p-3 rounded text-body-sm flex items-start gap-2" role="alert">
            <span class="material-symbols-outlined text-sm mt-0.5" aria-hidden="true">error</span>
            <p>{formError}</p>
          </div>
        {/if}

        <form on:submit={handleFormSubmit} class="flex flex-col gap-4">
          <div>
            <label for="protocol-title" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
              Title <span class="text-error" aria-hidden="true">*</span>
            </label>
            <input
              id="protocol-title"
              type="text"
              bind:value={formData.title}
              required
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              placeholder="e.g., Standard Protocol for Influenza"
            />
          </div>

          <div>
            <label for="protocol-category" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">Category</label>
            <select
              id="protocol-category"
              bind:value={formData.category}
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
            >
              <option value="Respiratory">Respiratory</option>
              <option value="Vector-Borne">Vector-Borne</option>
              <option value="Enteric">Enteric</option>
            </select>
          </div>

          <div>
            <label for="protocol-summary" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">Summary</label>
            <textarea
              id="protocol-summary"
              bind:value={formData.summary}
              rows="3"
              class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface"
              placeholder="Brief description of the protocol..."
            ></textarea>
          </div>

          <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
            <button
              type="button"
              on:click={closeForm}
              class="px-4 py-2 border border-outline-variant rounded-DEFAULT hover:bg-surface-container-high font-label-md text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSaving}
              class="px-4 py-2 bg-primary text-on-primary rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container font-label-md transition-colors focus:ring-2 focus:ring-primary focus:outline-none flex items-center gap-2 disabled:opacity-50"
            >
              {#if isSaving}
                <span class="material-symbols-outlined animate-spin text-sm" aria-hidden="true">progress_activity</span>
                Saving...
              {:else}
                Save Protocol
              {/if}
            </button>
          </div>
        </form>
      </div>
    </div>
  {/if}

  <!-- Delete Confirmation Modal -->
  {#if itemToDelete}
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-dialog-title"
    >
      <div class="bg-surface-container-lowest border border-outline-variant rounded-lg max-w-md w-full p-6 shadow-xl flex flex-col gap-4">
        <div class="flex items-center gap-3 text-error">
          <span class="material-symbols-outlined text-2xl" aria-hidden="true">warning</span>
          <h2 id="delete-dialog-title" class="font-headline-sm text-headline-sm font-bold text-on-surface">
            Confirm Deletion
          </h2>
        </div>
        <p class="font-body-md text-on-surface-variant">
          Are you sure you want to delete <strong class="text-on-surface">"{itemToDelete.title}"</strong>? This action cannot be undone.
        </p>
        <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
          <button
            type="button"
            on:click={cancelDelete}
            class="px-4 py-2 border border-outline-variant rounded-DEFAULT hover:bg-surface-container-high font-label-md text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none"
          >
            Cancel
          </button>
          <button
            type="button"
            on:click={executeDelete}
            class="px-4 py-2 bg-error text-on-error rounded-DEFAULT hover:opacity-90 font-label-md transition-colors focus:ring-2 focus:ring-error focus:outline-none"
          >
            Delete Item
          </button>
        </div>
      </div>
    </div>
  {/if}
</div>
