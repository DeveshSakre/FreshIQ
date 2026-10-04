import { PredictionResponse, ScanHistoryItem } from '../types/prediction';

const STORAGE_KEY = 'freshiq_scan_history_v1';
const MAX_HISTORY_ITEMS = 50;

/**
 * Service to manage scan history persistence.
 * Currently backed by browser localStorage, encapsulated behind this service interface
 * so it can easily be swapped for a backend API / database service in the future.
 */
export const historyService = {
  /**
   * Retrieves all saved scan records from local storage.
   */
  getHistory(): ScanHistoryItem[] {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return [];
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed)) return [];
      return parsed;
    } catch {
      return [];
    }
  },

  /**
   * Saves a new scan result to local storage.
   */
  saveScan(result: PredictionResponse, thumbnailUrl: string, storageCondition?: string): ScanHistoryItem {
    const history = this.getHistory();

    const newItem: ScanHistoryItem = {
      id: `scan-${Date.now()}-${Math.random().toString(36).substring(2, 6)}`,
      timestamp: new Date().toISOString(),
      thumbnailUrl: thumbnailUrl || '',
      result,
      storageCondition: storageCondition || 'ambient',
    };

    // Prepend new item and enforce maximum history limit
    const updated = [newItem, ...history.filter((item) => item.id !== newItem.id)].slice(0, MAX_HISTORY_ITEMS);

    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    } catch {
      // Storage quota or private browsing fallback:
      // Try stripping large base64 thumbnails if quota exceeded
      try {
        const lightweight = updated.map((item) => ({
          ...item,
          thumbnailUrl: item.thumbnailUrl.length > 5000 ? '' : item.thumbnailUrl,
        }));
        localStorage.setItem(STORAGE_KEY, JSON.stringify(lightweight));
      } catch {
        // Quota exceeded even without large thumbnails
      }
    }

    return newItem;
  },

  /**
   * Removes a single scan item by ID.
   */
  deleteScan(id: string): void {
    const history = this.getHistory();
    const updated = history.filter((item) => item.id !== id);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(updated));
    } catch {
      // Ignore
    }
  },

  /**
   * Clears all saved scan history.
   */
  clearHistory(): void {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // Ignore
    }
  },

  /**
   * Finds a scan by ID.
   */
  getScanById(id: string): ScanHistoryItem | null {
    const history = this.getHistory();
    return history.find((item) => item.id === id) || null;
  },
};
