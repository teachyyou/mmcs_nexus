import { createTheme } from '@mui/material/styles';

const common = {
    shape: { borderRadius: 16 },
    typography: {
        fontFamily: ['Inter', 'system-ui', 'Segoe UI', 'Roboto', 'Arial'].join(','),
        h1: { fontWeight: 700, fontSize: '2rem' },
        h2: { fontWeight: 700, fontSize: '1.5rem' },
        button: { textTransform: 'none', fontWeight: 600 },
    },
    components: {
        MuiAppBar: { styleOverrides: { root: { boxShadow: 'none' } } },
        MuiPaper: { defaultProps: { elevation: 0 } },
        MuiButton: { defaultProps: { disableElevation: true } },
        MuiModal: {
            defaultProps: {
                disableScrollLock: true,
            },
        },
        MuiMenu: {
            defaultProps: {
                disableScrollLock: true,
                anchorOrigin: { vertical: 'bottom', horizontal: 'right' },
                transformOrigin: { vertical: 'top', horizontal: 'right' },
                marginThreshold: 12,
            },
        },
    },
};

export function getTheme(mode = 'light') {
    if (mode === 'dark') {
        return createTheme({
            ...common,
            palette: {
                mode: 'dark',
                primary: { main: '#8B93FF', contrastText: '#ffffff' }, // сине-фиолетовый
                secondary: { main: '#A78BFA' },
                background: { default: '#0B0F19', paper: '#111827' },
            },
        });
    }
    return createTheme({
        ...common,
        palette: {
            mode: 'light',
            primary: { main: '#5B5BD6', contrastText: '#ffffff' },   // сине-фиолетовый
            secondary: { main: '#7C3AED' },
            background: { default: '#F7F7FB', paper: '#FFFFFF' },
        },
    });
}
