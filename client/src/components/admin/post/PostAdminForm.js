import React, { useRef, useState } from 'react';
import {
    BooleanInput,
    required,
    SaveButton,
    SimpleForm,
    TextInput,
    Toolbar,
    useNotify,
    useRecordContext,
    useRedirect,
    useRefresh,
} from 'react-admin';
import { useFormContext } from 'react-hook-form';
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Snackbar,
    Typography,
} from '@mui/material';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import HtmlEditorInput from './HtmlEditorInput';

const PostFormToolbar = ({ mode, record }) => {
    const notify = useNotify();
    const refresh = useRefresh();
    const isEditMode = mode === 'edit';

    const handlePublicationChange = async () => {
        if (!record?.id) return;

        const nextPublished = !record.published;

        try {
            const response = await fetch(`/api/v1/admin/posts/${record.id}`, {
                method: 'PATCH',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    published: nextPublished,
                }),
            });

            if (!response.ok) {
                throw new Error('Не удалось изменить статус публикации');
            }

            notify(nextPublished ? 'Пост опубликован' : 'Публикация отменена');
            refresh();
        } catch (error) {
            notify(error.message || 'Ошибка публикации', { type: 'error' });
        }
    };

    return (
        <Toolbar sx={{ display: 'flex', alignItems: 'center', gap: 1, px: 0 }}>
            <SaveButton label={isEditMode ? 'Сохранить' : 'Создать'} />

            <Box sx={{ flex: 1 }} />

            {isEditMode && (
                <Button
                    variant={record?.published  ? 'outlined' : 'contained'}
                    color={record?.published  ? 'warning' : 'success'}
                    onClick={handlePublicationChange}
                >
                    {record?.published  ? 'Отменить публикацию' : 'Опубликовать'}
                </Button>
            )}
        </Toolbar>
    );
};

const BannerUploadInput = ({
    isEditMode,
    record,
    bannerPreviewUrl,
    setBannerPreviewUrl,
    uploading,
    setUploading,
    setSnackbar,
}) => {
    const fileInputRef = useRef(null);
    const { setValue } = useFormContext();

    const openFilePicker = () => fileInputRef.current?.click();

    const handleBannerSelected = async (event) => {
        const file = event.target.files?.[0];
        event.target.value = '';

        if (!file) return;

        if (!file.type?.startsWith('image/')) {
            setSnackbar({
                open: true,
                msg: 'Можно загрузить только изображение',
                severity: 'warning',
            });
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            setUploading(true);

            const response = await fetch('/api/v1/admin/upload', {
                method: 'POST',
                credentials: 'include',
                body: formData,
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({}));
                throw new Error(error.error || error.message || 'Не удалось загрузить изображение');
            }

            const data = await response.json();
            const uploadedId = data.uploadId ?? data.id ?? data.fileId;

            if (!uploadedId) {
                throw new Error('Сервер не вернул id загруженного файла');
            }

            setValue('bannerFileId', uploadedId, {
                shouldDirty: true,
                shouldTouch: true,
                shouldValidate: true,
            });

            setBannerPreviewUrl(URL.createObjectURL(file));

            setSnackbar({
                open: true,
                msg: 'Изображение загружено',
                severity: 'success',
            });
        } catch (error) {
            setSnackbar({
                open: true,
                msg: error.message || 'Ошибка загрузки изображения',
                severity: 'error',
            });
        } finally {
            setUploading(false);
        }
    };

    const previewUrl = bannerPreviewUrl || record?.bannerUrl;

    return (
        <Box sx={{ width: '100%' }}>
            <Typography variant="subtitle2" sx={{ mb: 1 }}>
                Баннер поста
            </Typography>

            <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                style={{ display: 'none' }}
                onChange={handleBannerSelected}
            />

            <Button
                variant="contained"
                size="small"
                startIcon={uploading ? <CircularProgress size={16} /> : <UploadFileIcon />}
                onClick={openFilePicker}
                disabled={uploading}
            >
                {uploading ? 'Загрузка…' : isEditMode ? 'Заменить изображение' : 'Загрузить изображение'}
            </Button>

            {previewUrl && (
                <Box
                    component="img"
                    src={previewUrl}
                    alt={record?.title || 'Предпросмотр баннера'}
                    sx={{
                        display: 'block',
                        mt: 2,
                        width: '100%',
                        maxWidth: 480,
                        maxHeight: 270,
                        objectFit: 'cover',
                        borderRadius: 2,
                    }}
                />
            )}
        </Box>
    );
};

const PostAdminForm = ({ requestMethod, mode = 'create' }) => {
    const notify = useNotify();
    const redirect = useRedirect();
    const record = useRecordContext();

    const isEditMode = mode === 'edit';

    const [bannerPreviewUrl, setBannerPreviewUrl] = useState(null);
    const [uploading, setUploading] = useState(false);
    const [snackbar, setSnackbar] = useState({
        open: false,
        msg: '',
        severity: 'success',
    });

    const requiredMsg = required('Обязательное поле');

    const handleCloseSnackbar = () => {
        setSnackbar((current) => ({ ...current, open: false }));
    };

    const handleSubmit = async (data) => {
        const selectedBannerFileId = data.bannerFileId || record?.bannerFileId;

        if (!selectedBannerFileId) {
            notify('Загрузите изображение для поста', { type: 'warning' });
            return;
        }

        const dto = {
            title: data.title,
            previewText: data.previewText,
            contentHtml: data.contentHtml,
            bannerFileId: selectedBannerFileId,
            published: isEditMode ? Boolean(record?.published) : Boolean(data.published),
        };

        requestMethod('posts', isEditMode ? { id: record.id, data: dto } : { data: dto })
            .then(() => {
                notify(isEditMode ? 'Пост сохранён' : 'Пост создан');
                redirect('list', 'posts');
            })
            .catch((error) => {
                notify(error?.message || 'Не удалось сохранить пост', { type: 'error' });
            });
    };

    return (
        <>
            <SimpleForm
                onSubmit={handleSubmit}
                toolbar={<PostFormToolbar mode={mode} record={record} />}
                sx={{
                    maxWidth: 900,
                    width: '100%',
                    '& .MuiFormControl-root': { width: '100%' },
                    '& .RaInput-root': { width: '100%' },
                    gap: 2,
                }}
            >
                <TextInput
                    source="title"
                    label="Заголовок"
                    validate={requiredMsg}
                    inputProps={{ maxLength: 255 }}
                />

                <TextInput
                    source="previewText"
                    label="Анонс"
                    multiline
                    minRows={3}
                    maxRows={6}
                    inputProps={{ maxLength: 1024 }}
                />

                <HtmlEditorInput
                    source="contentHtml"
                    label="Содержимое поста"
                    validate={requiredMsg}
                />

                <TextInput
                    source="bannerFileId"
                    defaultValue={record?.bannerFileId}
                    sx={{ display: 'none' }}
                />

                {!isEditMode && (
                    <BooleanInput
                        source="published"
                        label="Сразу опубликовать"
                        defaultValue={false}
                    />
                )}

                <BannerUploadInput
                    isEditMode={isEditMode}
                    record={record}
                    bannerPreviewUrl={bannerPreviewUrl}
                    setBannerPreviewUrl={setBannerPreviewUrl}
                    uploading={uploading}
                    setUploading={setUploading}
                    setSnackbar={setSnackbar}
                />
            </SimpleForm>

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

export default PostAdminForm;