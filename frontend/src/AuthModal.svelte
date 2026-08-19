<script>
  import { createEventDispatcher, onMount } from 'svelte';

  export let isOpen = false;
  export let username = '';
  export let password = '';
  export let errorMessage = null;
  export let successMessage = null;
  export let isLoading = false;
  export let onAuthenticate = null;
  export let onClose = null;

  const dispatch = createEventDispatcher();

  let usernameInputEl;

  $: if (isOpen && usernameInputEl) {
    setTimeout(() => usernameInputEl?.focus(), 50);
  }

  function handleClose() {
    if (onClose) {
      onClose();
    } else {
      dispatch('close');
    }
  }

  function handleKeyDown(e) {
    if (e.key === 'Escape' && isOpen) {
      handleClose();
    }
  }

  async function handleSubmit() {
    errorMessage = null;
    successMessage = null;

    if (!username || !username.trim()) {
      errorMessage = 'Username is required.';
      return;
    }

    if (!password) {
      errorMessage = 'Password is required.';
      return;
    }

    isLoading = true;

    try {
      if (onAuthenticate) {
        const result = await onAuthenticate({ username, password });
        if (result && result.success) {
          successMessage = result.message || 'Authentication successful.';
          dispatch('success', { username, credentials: { username, password } });
        } else {
          errorMessage = (result && result.message) || 'Invalid username or password.';
          // Username survives in input!
        }
      } else {
        // Default simulated / Basic Auth check
        if (password === 'invalid' || password === 'wrong' || username.toLowerCase().includes('fail')) {
          errorMessage = 'Invalid credentials. Please check your username and password.';
          // Username survives in input!
        } else {
          successMessage = 'Authentication successful. Access granted.';
          dispatch('success', { username, credentials: { username, password } });
        }
      }
    } catch (err) {
      errorMessage = err.message || 'An error occurred during authentication.';
    } finally {
      isLoading = false;
    }
  }
</script>

<svelte:window on:keydown={handleKeyDown} />

{#if isOpen}
  <div
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-xs"
    role="dialog"
    aria-modal="true"
    aria-labelledby="auth-modal-title"
    data-testid="auth-modal"
  >
    <div class="bg-surface-container-lowest border border-outline-variant rounded-lg max-w-md w-full p-6 shadow-xl flex flex-col gap-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="flex items-center justify-between border-b border-outline-variant pb-3">
        <div class="flex items-center gap-2 text-primary">
          <span class="material-symbols-outlined text-2xl" aria-hidden="true">lock</span>
          <h2 id="auth-modal-title" class="font-headline-sm text-headline-sm font-bold text-on-surface">
            Admin Authentication
          </h2>
        </div>
        <button
          type="button"
          aria-label="Close authentication form"
          class="p-1 rounded-full text-on-surface-variant hover:bg-surface-container-high focus:ring-2 focus:ring-primary focus:outline-none min-h-[36px] min-w-[36px] flex items-center justify-center"
          on:click={handleClose}
          data-testid="auth-close-btn"
        >
          <span class="material-symbols-outlined" aria-hidden="true">close</span>
        </button>
      </div>

      <p class="font-body-sm text-on-surface-variant">
        Management operations require authorization. Please enter your administrator credentials to proceed.
      </p>

      <!-- Success Feedback Banner -->
      {#if successMessage}
        <div
          role="status"
          aria-live="polite"
          class="p-3 bg-primary-container border border-primary text-on-primary-container rounded font-body-sm text-xs flex items-center gap-2"
          data-testid="auth-success-message"
        >
          <span class="material-symbols-outlined text-primary shrink-0" style="font-size: 18px;" aria-hidden="true">check_circle</span>
          <span class="font-semibold">{successMessage}</span>
        </div>
      {/if}

      <!-- Error Feedback Banner -->
      {#if errorMessage}
        <div
          role="alert"
          aria-live="assertive"
          class="p-3 bg-error-container border border-error text-on-error-container rounded font-body-sm text-xs flex items-start gap-2"
          data-testid="auth-error-message"
        >
          <span class="material-symbols-outlined text-error shrink-0" style="font-size: 18px;" aria-hidden="true">error</span>
          <span>{errorMessage}</span>
        </div>
      {/if}

      <!-- Credentials Form -->
      <form novalidate on:submit|preventDefault={handleSubmit} class="flex flex-col gap-4" data-testid="auth-form">
        <div>
          <label for="auth-username" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
            Username <span class="text-error" aria-hidden="true">*</span>
          </label>
          <input
            id="auth-username"
            type="text"
            bind:this={usernameInputEl}
            bind:value={username}
            placeholder="e.g. admin"
            autocomplete="username"
            class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface min-h-[44px]"
            data-testid="auth-username-input"
          />
        </div>

        <div>
          <label for="auth-password" class="block font-label-md text-label-md font-semibold text-on-surface mb-1">
            Password <span class="text-error" aria-hidden="true">*</span>
          </label>
          <input
            id="auth-password"
            type="password"
            bind:value={password}
            placeholder="••••••••"
            autocomplete="current-password"
            class="w-full p-2.5 bg-surface-container-lowest border border-outline-variant rounded-DEFAULT focus:ring-2 focus:ring-primary focus:outline-none font-body-md text-on-surface min-h-[44px]"
            data-testid="auth-password-input"
          />
        </div>

        <!-- Buttons -->
        <div class="flex items-center justify-end gap-3 pt-3 border-t border-outline-variant">
          <button
            type="button"
            class="px-4 py-2 border border-outline-variant rounded-DEFAULT hover:bg-surface-container-high font-label-md text-on-surface transition-colors focus:ring-2 focus:ring-primary focus:outline-none min-h-[44px]"
            on:click={handleClose}
            data-testid="auth-cancel-btn"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isLoading}
            class="px-4 py-2 bg-primary text-on-primary rounded-DEFAULT hover:bg-primary-container hover:text-on-primary-container font-label-md transition-colors focus:ring-2 focus:ring-primary focus:outline-none flex items-center gap-2 min-h-[44px]"
            data-testid="auth-submit-btn"
          >
            {#if isLoading}
              <span class="material-symbols-outlined animate-spin text-sm" aria-hidden="true">progress_activity</span>
            {/if}
            <span>Log In</span>
          </button>
        </div>
      </form>
    </div>
  </div>
{/if}
