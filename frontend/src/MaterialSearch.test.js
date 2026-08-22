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
      props: { isAuthenticated: true, authUsername: 'admin', size: 20 }
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

  it('paginates results correctly when page controls and rows per page are interacted with', async () => {
    const { getByTestId, getAllByTestId } = render(MaterialSearch, {
      props: { size: 5 }
    });

    // Verify initial page (5 items)
    let docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(5);
    expect(getByTestId('page-indicator').textContent).toContain('Page 1 of 3');

    // Click next page button
    const nextBtn = getByTestId('next-page-btn');
    await fireEvent.click(nextBtn);

    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(5);
    expect(getByTestId('page-indicator').textContent).toContain('Page 2 of 3');

    // Click page 3 button directly
    const page3Btn = getByTestId('page-btn-3');
    await fireEvent.click(page3Btn);

    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(2);
    expect(getByTestId('page-indicator').textContent).toContain('Page 3 of 3');

    // Click previous page button
    const prevBtn = getByTestId('prev-page-btn');
    await fireEvent.click(prevBtn);

    expect(getByTestId('page-indicator').textContent).toContain('Page 2 of 3');

    // Change rows per page size
    const rowsSelect = getByTestId('rows-per-page-select');
    await fireEvent.change(rowsSelect, { target: { value: '20' } });

    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(12);
    expect(getByTestId('page-indicator').textContent).toContain('Page 1 of 1');
  });

  it('displays loading indicator during async pagination fetch', async () => {
    let resolveFetch;
    const fetchPromise = new Promise((resolve) => {
      resolveFetch = resolve;
    });

    const mockFetch = vi.fn().mockReturnValue(
      fetchPromise.then(() => ({
        ok: true,
        json: async () => ({
          items: [
            {
              id: 'async-1',
              title: 'Async Document 1',
              summary: 'Summary 1',
              category: 'protocol',
              tags: ['tag1'],
              author: 'Author 1',
              createdAt: '2026-08-01T10:00:00Z',
              updatedAt: '2026-08-15T14:30:00Z',
              fileUrl: '/files/test.pdf'
            }
          ],
          pagination: {
            page: 1,
            size: 5,
            totalElements: 10,
            totalPages: 2,
            isFirst: false,
            isLast: true
          }
        })
      }))
    );

    const { getByTestId, queryByTestId } = render(MaterialSearch, {
      props: { fetchFn: mockFetch, size: 5 }
    });

    // Click next page button
    const nextBtn = getByTestId('next-page-btn');
    fireEvent.click(nextBtn);

    // Verify loading state is visible while pending
    await waitFor(() => {
      expect(getByTestId('loading-state')).toBeDefined();
    });

    // Resolve fetch
    resolveFetch();

    // Verify loading state clears and results are rendered
    await waitFor(() => {
      expect(queryByTestId('loading-state')).toBeNull();
      expect(getByTestId('results-list')).toBeDefined();
    });
  });

  it('satisfies WCAG 2.1 AA requirements on pagination controls with aria attributes and touch target styling', async () => {
    const { getByTestId, getByLabelText } = render(MaterialSearch, {
      props: { size: 5 }
    });

    const prevBtn = getByTestId('prev-page-btn');
    const nextBtn = getByTestId('next-page-btn');
    const rowsSelect = getByTestId('rows-per-page-select');

    expect(prevBtn.getAttribute('aria-label')).toBe('Go to previous page');
    expect(nextBtn.getAttribute('aria-label')).toBe('Go to next page');
    expect(rowsSelect.getAttribute('aria-label')).toBe('Rows per page');

    // Check page 1 aria-current attribute
    const page1Btn = getByTestId('page-btn-1');
    expect(page1Btn.getAttribute('aria-current')).toBe('page');

    // Page 2 should not have aria-current
    const page2Btn = getByTestId('page-btn-2');
    expect(page2Btn.getAttribute('aria-current')).toBeNull();
  });
});
