import React, { Suspense } from 'react';
import ThemeModeProvider from './ThemeModeProvider';
import { AuthProvider } from './AuthContext';
import AppContent from './AppContent';

export default function App() {
    return (
        <ThemeModeProvider>
            <AuthProvider>
                <Suspense fallback={<div style={{ padding: 24 }}>Загрузка…</div>}>
                    <AppContent />
                </Suspense>
            </AuthProvider>
        </ThemeModeProvider>
    );
}
