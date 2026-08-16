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

  it('opens and submits protocol creation form via POST API', async () => {
    const mockFetch = vi.fn().mockImplementation(async (url, options = {}) => {
      if (options.method === 'POST') {
        const body = JSON.parse(options.body);
        return {
          ok: true,
          json: async () => ({
            id: 99,
            ...body,
            createdAt: '2026-08-16T12:00:00Z'
          })
        };
      }
      return {
        ok: true,
        json: async () => ({
          items: [
            {
              id: 99,
              code: 'EPI-PROTO-099',
              title: 'New Rabies Surveillance Protocol',
              category: 'Zoonotic',
              version: 'v1.0',
              status: 'DRAFT',
              summary: 'Summary of rabies outbreak response',
              authorOrganization: 'WHO',
              publicationYear: 2026,
              createdAt: '2026-08-16T12:00:00Z'
            }
          ],
          pagination: { page: 0, size: 20, totalElements: 1, totalPages: 1, isFirst: true, isLast: true }
        })
      };
    });

    const { getByTestId, getByText } = render(ProtocolSearch, {
      props: { fetchFn: mockFetch }
    });

    const addBtn = getByTestId('add-protocol-btn');
    await fireEvent.click(addBtn);

    expect(getByTestId('protocol-form-modal')).toBeTruthy();

    await fireEvent.input(getByTestId('protocol-code-input'), { target: { value: 'EPI-PROTO-099' } });
    await fireEvent.input(getByTestId('protocol-title-input'), { target: { value: 'New Rabies Surveillance Protocol' } });
    await fireEvent.change(getByTestId('protocol-category-input'), { target: { value: 'Zoonotic' } });
    await fireEvent.input(getByTestId('protocol-author-input'), { target: { value: 'WHO' } });

    const submitFormBtn = getByTestId('submit-protocol-form-btn');
    await fireEvent.click(submitFormBtn);

    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith('/api/v1/protocols', expect.objectContaining({ method: 'POST' }));
    });
  });

  it('preserves user input on submission failure', async () => {
    const { getByTestId, getByRole } = render(ProtocolSearch);

    const addBtn = getByTestId('add-protocol-btn');
    await fireEvent.click(addBtn);

    const codeInput = getByTestId('protocol-code-input');
    const titleInput = getByTestId('protocol-title-input');
    const summaryInput = getByTestId('protocol-summary-input');

    await fireEvent.input(codeInput, { target: { value: 'EPI-REJECT-001' } });
    await fireEvent.input(titleInput, { target: { value: 'Rejected Protocol Entry' } });
    await fireEvent.input(summaryInput, { target: { value: 'Important notes that must be preserved' } });

    const submitFormBtn = getByTestId('submit-protocol-form-btn');
    await fireEvent.click(submitFormBtn);

    await waitFor(() => {
      expect(getByTestId('protocol-form-error')).toBeTruthy();
      expect(getByRole('alert').textContent).toContain('Server rejected protocol submission');
    });

    // Inputs must be intact after submission failure
    expect(codeInput.value).toBe('EPI-REJECT-001');
    expect(titleInput.value).toBe('Rejected Protocol Entry');
    expect(summaryInput.value).toBe('Important notes that must be preserved');
  });

  it('allows opening delete modal and confirming protocol deletion', async () => {
    const mockFetch = vi.fn().mockImplementation(async (url, options = {}) => {
      if (options.method === 'DELETE') {
        return { ok: true, status: 204 };
      }
      return {
        ok: true,
        json: async () => ({
          items: [],
          pagination: { page: 0, size: 20, totalElements: 0, totalPages: 1, isFirst: true, isLast: true }
        })
      };
    });

    const { getByTestId, queryByTestId } = render(ProtocolSearch, {
      props: { fetchFn: mockFetch }
    });

    const deleteBtn = getByTestId('delete-protocol-btn-1');
    await fireEvent.click(deleteBtn);

    expect(getByTestId('delete-protocol-dialog')).toBeTruthy();

    const confirmBtn = getByTestId('confirm-delete-protocol-btn');
    await fireEvent.click(confirmBtn);

    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith('/api/v1/protocols/1', expect.objectContaining({ method: 'DELETE' }));
      expect(queryByTestId('delete-protocol-dialog')).toBeNull();
    });
  });
});
