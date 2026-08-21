import { render, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import MaterialSearch from './MaterialSearch.svelte';

describe('MaterialSearch component', () => {
  it('renders search bar, category chips, and initial sample documents', async () => {
    const { getByTestId, getAllByTestId } = render(MaterialSearch);

    expect(getByTestId('search-input')).toBeDefined();
    expect(getByTestId('search-submit')).toBeDefined();

    const docItems = getAllByTestId('document-item');
    expect(docItems.length).toBeGreaterThan(0);
  });

  it('filters documents when search query is entered and submitted', async () => {
    const { getByTestId, getAllByTestId } = render(MaterialSearch);

    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    await fireEvent.input(searchInput, { target: { value: 'influenza' } });
    await fireEvent.click(submitBtn);

    const docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(2);
  });

  it('shows explicit empty state with recovery suggestions when query yields no results', async () => {
    const { getByTestId } = render(MaterialSearch);

    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    await fireEvent.input(searchInput, { target: { value: 'nonexistentquery123' } });
    await fireEvent.click(submitBtn);

    expect(getByTestId('empty-state')).toBeDefined();

    // Test recovery button
    const resetBtn = getByTestId('reset-filters-btn');
    await fireEvent.click(resetBtn);

    expect(getByTestId('results-list')).toBeDefined();
  });

  it('prompts for credentials when unauthenticated user attempts management operations', async () => {
    const { getByTestId, queryByTestId } = render(MaterialSearch, {
      props: { isAuthenticated: false }
    });

    // Attempting Add Material prompts for credentials
    await fireEvent.click(getByTestId('add-material-btn'));

    expect(getByTestId('auth-modal')).toBeDefined();
    expect(queryByTestId('catalog-form-modal')).toBeNull();

    // Fill credentials and submit
    await fireEvent.input(getByTestId('auth-username-input'), { target: { value: 'admin' } });
    await fireEvent.input(getByTestId('auth-password-input'), { target: { value: 'secret' } });
    await fireEvent.click(getByTestId('auth-submit-btn'));

    // Authenticates and opens pending management action (create form)
    await waitFor(() => {
      expect(getByTestId('catalog-form-modal')).toBeDefined();
      expect(getByTestId('auth-feedback-banner')).toBeDefined();
    });
  });

  it('preserves user typed input when server/validation rejects form submission', async () => {
    const { getByTestId } = render(MaterialSearch, {
      props: { isAuthenticated: true, authUsername: 'admin' }
    });

    // Open create material modal
    await fireEvent.click(getByTestId('add-material-btn'));
    expect(getByTestId('catalog-form-modal')).toBeDefined();

    // Fill form with input that triggers simulated rejection (contains "reject")
    const titleInput = getByTestId('form-title-input');
    const summaryInput = getByTestId('form-summary-textarea');
    const authorInput = getByTestId('form-author-input');

    await fireEvent.input(titleInput, { target: { value: 'Rejected Material Title' } });
    await fireEvent.input(summaryInput, { target: { value: 'Detailed summary of the rejected material.' } });
    await fireEvent.input(authorInput, { target: { value: 'Dr. John Doe' } });

    // Submit form
    await fireEvent.click(getByTestId('form-submit-btn'));

    // Verify error message is shown
    expect(getByTestId('form-error-message')).toBeDefined();
    expect(getByTestId('form-error-message').textContent).toContain('Server rejected form submission');

    // Verify typed input survives in the form inputs
    expect(titleInput.value).toBe('Rejected Material Title');
    expect(summaryInput.value).toBe('Detailed summary of the rejected material.');
    expect(authorInput.value).toBe('Dr. John Doe');
  });

  it('opens confirmation dialog before irreversible item deletion when authenticated', async () => {
    const { getByTestId, queryByTestId, getAllByTestId } = render(MaterialSearch, {
      props: { isAuthenticated: true, authUsername: 'admin' }
    });

    const initialDocCount = getAllByTestId('document-item').length;
    const deleteBtn = getByTestId('delete-btn-123e4567-e89b-12d3-a456-426614174000');

    // Click delete
    await fireEvent.click(deleteBtn);

    // Confirmation dialog appears
    expect(getByTestId('delete-confirmation-dialog')).toBeDefined();

    // Cancel deletion
    await fireEvent.click(getByTestId('delete-cancel-btn'));
    expect(queryByTestId('delete-confirmation-dialog')).toBeNull();
    expect(getAllByTestId('document-item').length).toBe(initialDocCount);

    // Click delete again and confirm
    await fireEvent.click(deleteBtn);
    expect(getByTestId('delete-confirmation-dialog')).toBeDefined();

    await fireEvent.click(getByTestId('delete-confirm-btn'));
    expect(queryByTestId('delete-confirmation-dialog')).toBeNull();
    expect(getAllByTestId('document-item').length).toBe(initialDocCount - 1);
  });

  it('renders pagination controls and paginates through search results', async () => {
    const { getByTestId, getAllByTestId } = render(MaterialSearch, {
      props: { size: 2 }
    });

    // Wait for onMount state update to flush
    await waitFor(() => {
      expect(getByTestId('page-indicator').textContent).toContain('Page 1 of 3');
    });

    // Pagination bar exists
    expect(getByTestId('pagination-controls')).toBeDefined();

    // Currently showing 2 documents for page size 2
    let docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(2);

    // Prev button disabled on first page
    const prevBtn = getByTestId('prev-page-btn');
    const nextBtn = getByTestId('next-page-btn');
    expect(prevBtn.disabled).toBe(true);
    expect(nextBtn.disabled).toBe(false);

    // Click Next Page button
    await fireEvent.click(nextBtn);

    expect(getByTestId('page-indicator').textContent).toContain('Page 2 of 3');
    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(2);
    expect(prevBtn.disabled).toBe(false);

    // Click Page 3 direct button
    const page3Btn = getByTestId('page-btn-2');
    await fireEvent.click(page3Btn);

    expect(getByTestId('page-indicator').textContent).toContain('Page 3 of 3');
    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(1);
    expect(nextBtn.disabled).toBe(true);

    // Change rows per page size
    const sizeSelect = getByTestId('rows-per-page-select');
    await fireEvent.change(sizeSelect, { target: { value: '10' } });

    expect(getByTestId('page-indicator').textContent).toContain('Page 1 of 1');
    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(5);
  });

  it('passes page and size parameters when executing search via API endpoint', async () => {
    const requestedUrls = [];
    const fetchFn = vi.fn((url) => {
      requestedUrls.push(url);
      return Promise.resolve({
        ok: true,
        json: async () => ({
          items: [
            { id: 'item-10', title: 'API Material 10', category: 'protocol', tags: [] }
          ],
          pagination: {
            page: 1,
            size: 5,
            totalElements: 12,
            totalPages: 3,
            isFirst: false,
            isLast: false
          }
        })
      });
    });

    const { getByTestId } = render(MaterialSearch, {
      props: { fetchFn, size: 5, page: 0 }
    });

    // Execute search
    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    await fireEvent.input(searchInput, { target: { value: 'surveillance' } });
    await fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(fetchFn).toHaveBeenCalled();
    });

    const lastUrl = requestedUrls[requestedUrls.length - 1];
    expect(lastUrl).toContain('q=surveillance');
    expect(lastUrl).toContain('page=0');
    expect(lastUrl).toContain('size=5');
  });
});
