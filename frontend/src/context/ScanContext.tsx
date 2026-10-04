import React, { createContext, useContext, useState } from 'react';
import { PredictionResponse, ScanHistoryItem } from '../types/prediction';

interface ScanContextType {
  currentResult: PredictionResponse | null;
  currentImagePreview: string | null;
  setCurrentScan: (result: PredictionResponse, imagePreview: string, storageCondition?: string) => void;
  clearCurrentScan: () => void;
  history: ScanHistoryItem[];
  clearHistory: () => void;
  removeFromHistory: (id: string) => void;
}

const ScanContext = createContext<ScanContextType | undefined>(undefined);

import { historyService } from '../services/historyService';

export const ScanProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [history, setHistory] = useState<ScanHistoryItem[]>(() => historyService.getHistory());
  const [currentResult, setCurrentResult] = useState<PredictionResponse | null>(() => {
    const saved = historyService.getHistory();
    return saved.length > 0 ? saved[0].result : null;
  });
  const [currentImagePreview, setCurrentImagePreview] = useState<string | null>(() => {
    const saved = historyService.getHistory();
    return saved.length > 0 ? saved[0].thumbnailUrl : null;
  });

  const setCurrentScan = (result: PredictionResponse, imagePreview: string, storageCondition?: string) => {
    setCurrentResult(result);
    setCurrentImagePreview(imagePreview);

    // Save through historyService
    const saved = historyService.saveScan(result, imagePreview, storageCondition);
    setHistory((prev) => [saved, ...prev.filter((i) => i.id !== saved.id)]);
  };

  const clearCurrentScan = () => {
    setCurrentResult(null);
    setCurrentImagePreview(null);
  };

  const clearHistory = () => {
    historyService.clearHistory();
    setHistory([]);
  };

  const removeFromHistory = (id: string) => {
    historyService.deleteScan(id);
    setHistory((prev) => prev.filter((item) => item.id !== id));
  };

  return (
    <ScanContext.Provider
      value={{
        currentResult,
        currentImagePreview,
        setCurrentScan,
        clearCurrentScan,
        history,
        clearHistory,
        removeFromHistory
      }}
    >
      {children}
    </ScanContext.Provider>
  );
};

export const useScan = (): ScanContextType => {
  const context = useContext(ScanContext);
  if (!context) {
    throw new Error('useScan must be used within a ScanProvider');
  }
  return context;
};
