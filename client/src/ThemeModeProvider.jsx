import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { ThemeProvider, CssBaseline } from '@mui/material';
import { getTheme } from './theme';

const ThemeModeCtx = createContext({
    mode: 'light',
    toggleMode: () => {},
    setMode: () => {},
});

export function useThemeMode() {
    return useContext(ThemeModeCtx);
}

export default function ThemeModeProvider({ children }) {
    const readInitial = () => {
        const saved = typeof window !== 'undefined' ? localStorage.getItem('themeMode') : null;
        if (saved === 'light' || saved === 'dark') return saved;
        // если не сохранено — берём системную
        if (typeof window !== 'undefined' && window.matchMedia) {
            return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
        }
        return 'light';
    };

    const [mode, setMode] = useState(readInitial);

    useEffect(() => {
        localStorage.setItem('themeMode', mode);
    }, [mode]);

    const toggleMode = useCallback(() => {
        setMode((m) => (m === 'light' ? 'dark' : 'light'));
    }, []);

    const theme = useMemo(() => getTheme(mode), [mode]);

    const value = useMemo(() => ({ mode, setMode, toggleMode }), [mode, toggleMode]);

    return (
        <ThemeModeCtx.Provider value={value}>
            <ThemeProvider theme={theme}>
                <CssBaseline />
                {children}
            </ThemeProvider>
        </ThemeModeCtx.Provider>
    );
}
