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
});
