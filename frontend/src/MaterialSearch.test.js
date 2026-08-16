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

  it('displays error banner and retry button when download fails', async () => {
    const fetchFn = vi.fn().mockRejectedValue(new Error('Network connection dropped'));
    const { getByTestId, getAllByTestId } = render(MaterialSearch, {
      props: { fetchFn }
    });

    const docItems = getAllByTestId('document-item');
    expect(docItems.length).toBeGreaterThan(0);

    const downloadBtn = getByTestId('download-btn-123e4567-e89b-12d3-a456-426614174000');
    await fireEvent.click(downloadBtn);

    await waitFor(() => {
      expect(getByTestId('error-banner')).toBeDefined();
      expect(getByTestId('retry-btn')).toBeDefined();
    });
  });

  it('transmits telemetry events upon document click and abandonment', async () => {
    let mockTime = 1000;
    const nowFn = () => mockTime;
    const sentRequests = [];

    const fetchFn = vi.fn((url, options) => {
      if (options && options.body) {
        sentRequests.push({ url, body: JSON.parse(options.body) });
      }
      return Promise.resolve({ ok: true, json: async () => ({ items: [] }) });
    });

    const { getByTestId } = render(MaterialSearch, {
      props: { nowFn, fetchFn }
    });

    // Enter search query
    await fireEvent.input(getByTestId('search-input'), { target: { value: 'protocol' } });
    await fireEvent.click(getByTestId('search-submit'));

    expect(getByTestId('abandon-btn')).toBeDefined();

    // Advance time
    mockTime = 1500;

    // Abandon search
    const abandonBtn = getByTestId('abandon-btn');
    await fireEvent.click(abandonBtn);

    expect(sentRequests.some(r => r.body.eventType === 'SEARCH_ABANDONED')).toBe(true);
  });
});
