import { render, fireEvent, waitFor } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import ProtocolSearch from './ProtocolSearch.svelte';

describe('ProtocolSearch E2E Workflows', () => {
  it('allows end-to-end search, filtering, and resetting filters with mock API backend', async () => {
    const mockProtocols = [
      {
        id: 101,
        code: 'EPI-PROTO-101',
        title: 'Lassa Fever Surveillance Guidance',
        category: 'Viral Hemorrhagic Fevers',
        version: 'v1.1',
        status: 'APPROVED',
        summary: 'Standard operating procedures for rapid isolation and contact tracing.',
        authorOrganization: 'NCDC',
        publicationYear: 2025,
        createdAt: '2026-08-16T10:00:00Z'
      },
      {
        id: 102,
        code: 'EPI-PROTO-102',
        title: 'Mpox Community Containment Protocol',
        category: 'Zoonotic',
        version: 'v2.0',
        status: 'DRAFT',
        summary: 'Guidelines for contact tracing and ring vaccination ring strategy.',
        authorOrganization: 'Africa CDC',
        publicationYear: 2024,
        createdAt: '2026-08-15T10:00:00Z'
      }
    ];

    const mockFetch = vi.fn().mockImplementation(async (url) => {
      let q = '';
      let category = '';
      let status = '';
      if (typeof url === 'string' && url.includes('?')) {
        const queryStr = url.split('?')[1];
        const params = new URLSearchParams(queryStr);
        q = params.get('q') || '';
        category = params.get('category') || '';
        status = params.get('status') || '';
      }

      let filtered = [...mockProtocols];
      if (q) {
        filtered = filtered.filter(p => p.title.toLowerCase().includes(q.toLowerCase()) || p.code.toLowerCase().includes(q.toLowerCase()));
      }
      if (category) {
        filtered = filtered.filter(p => p.category === category);
      }
      if (status) {
        filtered = filtered.filter(p => p.status === status);
      }

      return {
        ok: true,
        json: async () => ({
          items: filtered,
          pagination: {
            page: 0,
            size: 20,
            totalElements: filtered.length,
            totalPages: 1,
            isFirst: true,
            isLast: true
          }
        })
      };
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
      expect(cards.length).toBe(2);
    });

    // 1. Enter query "Lassa"
    const searchInput = getByTestId('protocol-search-input');
    await fireEvent.input(searchInput, { target: { value: 'Lassa' } });
    await fireEvent.click(submitBtn);

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(1);
      expect(cards[0].textContent).toContain('Lassa Fever Surveillance Guidance');
    });

    // 2. Clear / Reset Filters
    const resetBtn = getByTestId('reset-filters-btn');
    await fireEvent.click(resetBtn);

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(2);
    });
  });

  it('displays error banner and allows retry on API failure', async () => {
    let callCount = 0;
    const mockFetch = vi.fn().mockImplementation(async () => {
      callCount++;
      if (callCount === 1) {
        return {
          ok: false,
          status: 500,
          json: async () => ({ message: 'Server temporarily unavailable' })
        };
      }
      return {
        ok: true,
        json: async () => ({
          items: [
            {
              id: 201,
              code: 'EPI-PROTO-201',
              title: 'Recovered Protocol Response',
              category: 'Respiratory',
              version: 'v1.0',
              status: 'APPROVED',
              summary: 'Summary text',
              authorOrganization: 'WHO',
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
      };
    });

    const { getByTestId, getAllByTestId, queryByRole, getByRole } = render(ProtocolSearch, {
      props: { fetchFn: mockFetch }
    });

    const submitBtn = getByTestId('search-submit-btn');
    await fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(getByRole('alert')).toBeTruthy();
      expect(getByRole('alert').textContent).toContain('Server temporarily unavailable');
    });

    const retryBtn = getByTestId('retry-btn');
    await fireEvent.click(retryBtn);

    await waitFor(() => {
      expect(queryByRole('alert')).toBeNull();
    });

    await waitFor(() => {
      const cards = getAllByTestId('protocol-card');
      expect(cards.length).toBe(1);
      expect(cards[0].textContent).toContain('Recovered Protocol Response');
    });
  });
});
