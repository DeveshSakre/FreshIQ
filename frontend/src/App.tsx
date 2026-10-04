import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ScanProvider } from './context/ScanContext';
import { AppLayout } from './layouts/AppLayout';
import { HomePage } from './pages/HomePage';
import { ScanPage } from './pages/ScanPage';
import { AnalysisPage } from './pages/AnalysisPage';
import { ComparisonPage } from './pages/ComparisonPage';
import { WhatIfPage } from './pages/WhatIfPage';
import { HistoryPage } from './pages/HistoryPage';
import { InsightsPage } from './pages/InsightsPage';

export const App: React.FC = () => {
  return (
    <BrowserRouter>
      <ScanProvider>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<HomePage />} />
            <Route path="/scan" element={<ScanPage />} />
            <Route path="/analysis" element={<AnalysisPage />} />
            <Route path="/comparison" element={<ComparisonPage />} />
            <Route path="/what-if" element={<WhatIfPage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="/insights" element={<InsightsPage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </ScanProvider>
    </BrowserRouter>
  );
};

export default App;
