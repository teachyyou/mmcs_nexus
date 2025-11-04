// ProjectList.js
import React, { useRef, useState } from 'react';
import {
    Datagrid,
    FunctionField,
    List,
    NumberField,
    TextField,
    TopToolbar,
    useRefresh,
    CreateButton,
} from 'react-admin';
import { Button, CircularProgress, Snackbar, Alert, Tooltip } from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';

const renderProjectType = (record) => {
    switch (String(record.type)) {
        case 'WEB_APP': return 'Веб-приложение';
        case 'DESKTOP_APP': return 'Десктопное приложение';
        case 'MOBILE_APP': return 'Мобильное приложение';
        case 'GAME': return 'Игра';
        case 'GAME_MOD': return 'Мод';
        case 'TELEGRAM_BOT': return 'Телеграм-бот';
        default: return record?.type ?? '-';
    }
};

const ImportProjectsButton = () => {
    const refresh = useRefresh();
    const fileInputRef = useRef(null);
    const [uploading, setUploading] = useState(false);
    const [snackbar, setSnackbar] = useState({ open: false, msg: '', severity: 'success' });

    const openPicker = () => fileInputRef.current?.click();
    const handleCloseSnackbar = () => setSnackbar((s) => ({ ...s, open: false }));

    const handleFileSelected = async (e) => {
        const file = e.target.files?.[0];
        e.target.value = '';
        if (!file) return;

        const validExt = /\.(csv)$/i.test(file.name);
        const validMime = [
            'text/csv',
            'application/vnd.ms-excel',
            'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        ].includes(file.type);

        if (!validExt && !validMime) {
            setSnackbar({ open: true, msg: 'Поддерживается только CSV', severity: 'warning' });
            return;
        }

        const form = new FormData();
        form.append('file', file);

        try {
            setUploading(true);
            const res = await fetch('/api/v1/admin/projects/from_csv?limit=100', {
                method: 'POST',
                credentials: 'include',
                body: form,
            });

            if (!res.ok) {
                const err = await res.json().catch(() => ({}));
                throw new Error(err.error || 'Импорт завершился с ошибкой');
            }

            setSnackbar({ open: true, msg: 'Импорт выполнен', severity: 'success' });
            refresh();
        } catch (err) {
            setSnackbar({ open: true, msg: err.message || 'Ошибка импорта', severity: 'error' });
        } finally {
            setUploading(false);
        }
    };

    return (
        <>
            <input
                ref={fileInputRef}
                type="file"
                accept=".csv, text/csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                style={{ display: 'none' }}
                onChange={handleFileSelected}
            />
            <Tooltip title="Импортировать проекты из CSV">
                <span>
                    <Button
                        onClick={openPicker}
                        variant="contained"
                        size="small"
                        startIcon={uploading ? <CircularProgress size={16} /> : <UploadFileIcon />}
                        disabled={uploading}
                        sx={{ ml: 1 }}
                    >
                        {uploading ? 'Импорт…' : 'Импорт CSV'}
                    </Button>
                </span>
            </Tooltip>

            <Snackbar
                open={snackbar.open}
                autoHideDuration={3000}
                onClose={handleCloseSnackbar}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
                    {snackbar.msg}
                </Alert>
            </Snackbar>
        </>
    );
};

const ProjectListActions = () => (
    <TopToolbar sx={{ pl: 0, gap: 1 }}>
        <CreateButton label="Создать" />
        <ImportProjectsButton />
    </TopToolbar>
);

const ProjectList = (props) => (
    <List {...props} exporter={false} actions={<ProjectListActions />}>
        <Datagrid rowClick="edit">
            <NumberField source="externalId" label="Внешний ID" textAlign="center" />
            <TextField source="name" label="Название" />
            <TextField source="description" label="Описание" />
            <FunctionField source="type" label="Тип" render={renderProjectType} />
            <NumberField source="quantityOfStudents" label="Число участников" />
            <TextField source="track" label="Трек" />
        </Datagrid>
    </List>
);

export default ProjectList;
