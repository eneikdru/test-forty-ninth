import { render, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import MaterialSearch from './MaterialSearch.svelte';

describe('MaterialSearch E2E Flow', () => {
  it('allows user to perform search, filter materials, and view/download document end-to-end', async () => {
    const downloadedUrls = [];
    const fetchFn = vi.fn((url, options) => {
      if (url.includes('/files/')) {
        downloadedUrls.push(url);
        return Promise.resolve({
          ok: true,
          blob: async () => new Blob(['dummy document content'], { type: 'application/pdf' })
        });
      }
      return Promise.resolve({ ok: true, json: async () => ({ items: [] }) });
    });

    const { getByTestId, getAllByTestId, queryByTestId } = render(MaterialSearch, {
      props: { fetchFn }
    });

    // 1. Initial view shows all default epidemiological materials
    let docItems = getAllByTestId('document-item');
    expect(docItems.length).toBeGreaterThan(0);

    // 2. Perform search for "Ebola"
    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    await fireEvent.input(searchInput, { target: { value: 'Ebola' } });
    await fireEvent.click(submitBtn);

    // 3. Verify filtered results list displays Ebola guidelines
    docItems = getAllByTestId('document-item');
    expect(docItems.length).toBe(1);
    expect(docItems[0].textContent).toContain('Ebola Response Guidelines v2.1');

    // 4. User views / downloads document
    const ebolaDocId = '423e4567-e89b-12d3-a456-426614174003';
    const downloadBtn = getByTestId(`download-btn-${ebolaDocId}`);
    await fireEvent.click(downloadBtn);

    await waitFor(() => {
      expect(downloadedUrls).toContain('/files/guidelines/ebola-response-v2.1.pdf');
    });

    // 5. Verify telemetry logged click event
    expect(queryByTestId('telemetry-log')).not.toBeNull();
  });

  it('allows an administrator to successfully upload, edit, and delete a document', async () => {
    const { getByTestId, queryByTestId, queryByText, getByText } = render(MaterialSearch);

    // 1. Upload / Add a new document
    const addBtn = getByTestId('add-material-btn');
    await fireEvent.click(addBtn);

    expect(getByTestId('catalog-form-modal')).not.toBeNull();

    const titleInput = getByTestId('form-title-input');
    const authorInput = getByTestId('form-author-input');
    const tagsInput = getByTestId('form-tags-input');
    const summaryTextarea = getByTestId('form-summary-textarea');
    const submitFormBtn = getByTestId('form-submit-btn');

    await fireEvent.input(titleInput, { target: { value: 'Novel Nipah Virus Containment Protocol' } });
    await fireEvent.input(authorInput, { target: { value: 'Dr. John Doe' } });
    await fireEvent.input(tagsInput, { target: { value: 'nipah, outbreak, protocol' } });
    await fireEvent.input(summaryTextarea, { target: { value: 'Guidelines for managing Nipah virus outbreaks.' } });

    await fireEvent.click(submitFormBtn);

    // Verify creation
    await waitFor(() => {
      expect(queryByTestId('catalog-form-modal')).toBeNull();
    });
    expect(getByText('Novel Nipah Virus Containment Protocol')).not.toBeNull();

    // 2. Edit the document
    // Get the newly created item's edit button or find by testid
    const documentTitleEl = getByText('Novel Nipah Virus Containment Protocol');
    const article = documentTitleEl.closest('article');
    const docId = article.getAttribute('data-doc-id');

    const editBtn = getByTestId(`edit-btn-${docId}`);
    await fireEvent.click(editBtn);

    expect(getByTestId('catalog-form-modal')).not.toBeNull();
    const editTitleInput = getByTestId('form-title-input');
    await fireEvent.input(editTitleInput, { target: { value: 'Updated Nipah Virus Containment Protocol v2' } });

    const editSubmitBtn = getByTestId('form-submit-btn');
    await fireEvent.click(editSubmitBtn);

    await waitFor(() => {
      expect(queryByTestId('catalog-form-modal')).toBeNull();
    });
    expect(getByText('Updated Nipah Virus Containment Protocol v2')).not.toBeNull();

    // 3. Delete the document
    const deleteBtn = getByTestId(`delete-btn-${docId}`);
    await fireEvent.click(deleteBtn);

    expect(getByTestId('delete-confirmation-dialog')).not.toBeNull();
    const confirmDeleteBtn = getByTestId('delete-confirm-btn');
    await fireEvent.click(confirmDeleteBtn);

    await waitFor(() => {
      expect(queryByTestId('delete-confirmation-dialog')).toBeNull();
    });
    expect(queryByText('Updated Nipah Virus Containment Protocol v2')).toBeNull();
  });
});
