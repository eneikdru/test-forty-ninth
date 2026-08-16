import { render, fireEvent } from '@testing-library/svelte';
import { describe, it, expect, vi } from 'vitest';
import SearchTelemetry from './SearchTelemetry.svelte';

describe('SearchTelemetry component', () => {
  it('transmits exact elapsed time when a search completes and a document is clicked', async () => {
    let mockTime = 1000;
    const nowFn = () => mockTime;
    const sentRequests = [];
    const fetchFn = vi.fn(async (url, options) => {
      sentRequests.push({ url, body: JSON.parse(options.body) });
      return { ok: true };
    });

    const { getByTestId, getAllByTestId } = render(SearchTelemetry, {
      props: {
        nowFn,
        fetchFn,
        trackingEndpoint: '/api/v1/analytics/search-events'
      }
    });

    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    // 1. Enter query and execute search at time = 1000ms
    await fireEvent.input(searchInput, { target: { value: 'protocol' } });
    await fireEvent.click(submitBtn);

    // 2. Advance time by 350ms to time = 1350ms
    mockTime = 1350;

    // 3. Click the first returned document item
    const docItems = getAllByTestId('document-item');
    expect(docItems.length).toBeGreaterThan(0);
    await fireEvent.click(docItems[0]);

    // 4. Verify telemetry transmission
    expect(sentRequests.length).toBe(1);
    const event = sentRequests[0].body;

    expect(event.eventType).toBe('SEARCH_CLICK');
    expect(event.query).toBe('protocol');
    expect(event.elapsedTimeMs).toBe(350); // Exact elapsed time: 1350 - 1000 = 350ms
    expect(event.documentId).toBe('123e4567-e89b-12d3-a456-426614174000');
  });

  it('transmits failed search event with query string when user abandons search via window beforeunload', async () => {
    let mockTime = 2000;
    const nowFn = () => mockTime;
    const sentRequests = [];
    const fetchFn = vi.fn(async (url, options) => {
      sentRequests.push({ url, body: JSON.parse(options.body) });
      return { ok: true };
    });

    const { getByTestId } = render(SearchTelemetry, {
      props: {
        nowFn,
        fetchFn,
        trackingEndpoint: '/api/v1/analytics/search-events'
      }
    });

    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    // 1. Enter query yielding results
    await fireEvent.input(searchInput, { target: { value: 'influenza' } });
    await fireEvent.click(submitBtn);

    // 2. Trigger window beforeunload event
    await fireEvent(window, new Event('beforeunload'));

    // 3. Verify failed search event was recorded and transmitted
    expect(sentRequests.length).toBe(1);
    const event = sentRequests[0].body;

    expect(event.eventType).toBe('SEARCH_ABANDONED');
    expect(event.query).toBe('influenza');
    expect(event.status).toBe('FAILED');
    expect(event.resultCount).toBeGreaterThan(0);
  });

  it('transmits failed search event when search yields no results and user clicks Abandon button', async () => {
    let mockTime = 3000;
    const nowFn = () => mockTime;
    const sentRequests = [];
    const fetchFn = vi.fn(async (url, options) => {
      sentRequests.push({ url, body: JSON.parse(options.body) });
      return { ok: true };
    });

    const { getByTestId } = render(SearchTelemetry, {
      props: {
        nowFn,
        fetchFn,
        trackingEndpoint: '/api/v1/analytics/search-events'
      }
    });

    const searchInput = getByTestId('search-input');
    const submitBtn = getByTestId('search-submit');

    // 1. Enter query yielding zero results
    await fireEvent.input(searchInput, { target: { value: 'nonexistentquery123' } });
    await fireEvent.click(submitBtn);

    expect(getByTestId('no-results')).toBeDefined();

    // 2. Click explicit Abandon Search button
    const abandonBtn = getByTestId('abandon-btn');
    await fireEvent.click(abandonBtn);

    // 3. Verify event
    expect(sentRequests.length).toBe(1);
    const event = sentRequests[0].body;

    expect(event.eventType).toBe('SEARCH_ABANDONED');
    expect(event.query).toBe('nonexistentquery123');
    expect(event.resultCount).toBe(0);
  });

  it('does not send multiple events if user clicks document then unmounts', async () => {
    let mockTime = 1000;
    const nowFn = () => mockTime;
    const sentRequests = [];
    const fetchFn = vi.fn(async (url, options) => {
      sentRequests.push({ url, body: JSON.parse(options.body) });
      return { ok: true };
    });

    const { getByTestId, getAllByTestId, unmount } = render(SearchTelemetry, {
      props: { nowFn, fetchFn }
    });

    await fireEvent.input(getByTestId('search-input'), { target: { value: 'protocol' } });
    await fireEvent.click(getByTestId('search-submit'));

    mockTime = 1200;
    const docItems = getAllByTestId('document-item');
    await fireEvent.click(docItems[0]);

    // Unmount after document click
    unmount();

    // Still only 1 SEARCH_CLICK event, no duplicate SEARCH_ABANDONED
    expect(sentRequests.length).toBe(1);
    expect(sentRequests[0].body.eventType).toBe('SEARCH_CLICK');
  });
});
