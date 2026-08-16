import { render, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import ProtocolSearch from './ProtocolSearch.svelte';

describe('ProtocolSearch Component', () => {
  it('renders correctly with initial protocols', () => {
    const { getByTestId, getAllByTestId } = render(ProtocolSearch);
    expect(getByTestId('protocol-search-input')).toBeTruthy();
    expect(getByTestId('category-filter-select')).toBeTruthy();
    expect(getByTestId('status-filter-select')).toBeTruthy();

    const cards = getAllByTestId('protocol-card');
    expect(cards.length).toBeGreaterThan(0);
  });

  it('filters protocols by search query', async () => {
    const { getByTestId, getAllByTestId } = render(ProtocolSearch);
    const input = getByTestId('protocol-search-input');
    const submitBtn = getByTestId('search-submit-btn');

    await fireEvent.input(input, { target: { value: 'COVID-19' } });
    await fireEvent.click(submitBtn);

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(1);
      expect(cards[0].textContent).toContain('COVID-19');
    });
  });

  it('filters protocols by category', async () => {
    const { getByTestId, getAllByTestId } = render(ProtocolSearch);
    const categorySelect = getByTestId('category-filter-select');

    await fireEvent.change(categorySelect, { target: { value: 'Vector-Borne' } });

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(1);
      expect(cards[0].textContent).toContain('Dengue');
    });
  });

  it('filters protocols by status', async () => {
    const { getByTestId, getAllByTestId } = render(ProtocolSearch);
    const statusSelect = getByTestId('status-filter-select');

    await fireEvent.change(statusSelect, { target: { value: 'DRAFT' } });

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(1);
      expect(cards[0].textContent).toContain('Cholera');
    });
  });

  it('calls fetchFn when querying protocols API', async () => {
    const mockFetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        items: [
          {
            id: 10,
            code: 'EPI-PROTO-010',
            title: 'Mock API Protocol Title',
            category: 'Respiratory',
            version: 'v1.0',
            status: 'APPROVED',
            summary: 'Mock Summary',
            authorOrganization: 'CDC',
            publicationYear: 2026,
            createdAt: '2026-08-16T12:00:00Z'
          }
        ],
        pagination: {
          page: 0,
          size: 20,
          totalElements: 1,
          totalPages: 1,
          isFirst: true,
          isLast: true
        }
      })
    });

    const { getByTestId, getAllByTestId } = render(ProtocolSearch, {
      props: { fetchFn: mockFetch }
    });

    const submitBtn = getByTestId('search-submit-btn');
    await fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalled();
    });

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(1);
      expect(cards[0].textContent).toContain('Mock API Protocol Title');
    });
  });
});
