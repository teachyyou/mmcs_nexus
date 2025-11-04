// client/src/components/admin/AdminLayout.jsx
import React from 'react';
import { Layout, useSidebarState } from 'react-admin';
import { Box } from '@mui/material';

const APPBAR_H = 64;
const EmptyAppBar = () => null;

export default function AdminLayout(props) {
    const [sidebarOpen, setSidebarOpen] = useSidebarState();

    // мост от глобального NavigationBar
    React.useEffect(() => {
        const handler = () => setSidebarOpen(!sidebarOpen);
        window.addEventListener('ra-toggle-sidebar', handler);
        return () => window.removeEventListener('ra-toggle-sidebar', handler);
    }, [sidebarOpen, setSidebarOpen]);

    return (
        <Box
            sx={{
                /* ВАЖНО: сам фиксированный сайдбар и его бумага должны быть сдвинуты вниз */
                '& .RaSidebar-fixed': {
                    top: `${APPBAR_H}px`,
                    height: `calc(100% - ${APPBAR_H}px)`,
                    zIndex: 1200,
                },
                '& .RaSidebar-fixed .MuiDrawer-paper': {
                    top: `${APPBAR_H}px`,
                    height: `calc(100% - ${APPBAR_H}px)`,
                },

                /* Контент админки: ровные отступы и maxWidth (верхний отступ делает уже body/глобально) */
                '& .RaLayout-content': {
                    pl: { xs: 2, md: 3 },
                    pr: { xs: 3, md: 5 },
                    pb: 3,
                    maxWidth: '1600px',
                    mx: 'auto',
                },

                '& .MuiCard-root': { mb: 3 },
            }}
        >
            <Layout {...props} appBar={EmptyAppBar} />
        </Box>
    );
}
