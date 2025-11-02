// ProjectAdminForm.js
import React from 'react';
import {
    BooleanInput,
    NumberInput,
    required,
    SelectInput,
    SimpleForm,
    TextInput,
    Toolbar,
    SaveButton,
    DeleteButton,
    useNotify,
    useRecordContext,
    useRedirect,
} from 'react-admin';
import { Box } from '@mui/material';

const ProjectFormToolbar = () => {
    const record = useRecordContext(); // есть только в Edit

    return (
        <Toolbar
            sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                px: 0,
            }}
        >
            {/* Сохранить — слева */}
            <SaveButton />

            {/* Заполнитель пространства */}
            <Box sx={{ flex: 1 }} />

            {/* Удалить — справа, только в режиме редактирования */}
            {record?.id && (
                <DeleteButton
                    mutationMode="pessimistic"
                    redirect="list"
                />
            )}
        </Toolbar>
    );
};

const ProjectAdminForm = ({ requestMethod }) => {
    const notify = useNotify();
    const redirect = useRedirect();
    const record = useRecordContext();

    // Ограничения ввода для числовых полей (как в EventAdminForm)
    const handleKeyDown = (e) => {
        if (e.key === '-' || e.key === 'e' || e.key === 'E') e.preventDefault();
    };
    const handlePaste = (e) => {
        const paste = e.clipboardData.getData('text');
        if (/[-eE]/.test(paste)) e.preventDefault();
    };
    const handleChange2Digits = (e) => {
        const orig = e.target.value;
        const cleaned = orig.replace(/[eE-]/g, '');
        if (orig !== cleaned) e.target.value = cleaned;
        else if (cleaned.length > 2) e.target.value = cleaned.slice(0, 2);
    };

    const yearNow = new Date().getFullYear();
    const requiredMsg = required('Обязательное поле');

    const handleSubmit = async (data) => {
        const { id, ...dto } = data;
        const params = record?.id ? { id: record.id, data: dto } : { data: dto };

        requestMethod('projects', params)
            .then(() => {
                notify('Сохранено успешно');
                redirect('list', 'projects');
            })
            .catch((error) => {
                if (error?.status === 409) {
                    notify('Проект с таким названием уже существует', { type: 'warning' });
                } else {
                    notify('Неизвестная ошибка', { type: 'error' });
                }
            });
    };

    return (
        <SimpleForm
            onSubmit={handleSubmit}
            toolbar={<ProjectFormToolbar />}
            sx={{
                maxWidth: 900,
                width: '100%',
                '& .MuiFormControl-root': { width: '100%' },
                '& .RaInput-root': { width: '100%' },
                gap: 2,
            }}
        >
            <TextInput
                source="name"
                label="Название"
                validate={requiredMsg}
                inputProps={{ maxLength: 64 }}
            />

            <TextInput
                source="description"
                label="Описание"
                validate={requiredMsg}
                multiline
                minRows={4}
                maxRows={10}
                inputProps={{ maxLength: 1024 }}
            />

            <NumberInput
                source="year"
                label="Год"
                min={yearNow - 5}
                max={yearNow + 5}
                defaultValue={record?.year ?? yearNow}
                validate={requiredMsg}
                inputProps={{ maxLength: 4, onKeyDown: handleKeyDown, onPaste: handlePaste }}
            />

            <SelectInput
                source="type"
                label="Тип проекта"
                choices={[
                    { id: 'WEB_APP', name: 'Веб-приложение' },
                    { id: 'DESKTOP_APP', name: 'Десктопное приложение' },
                    { id: 'MOBILE_APP', name: 'Мобильное приложение' },
                    { id: 'GAME', name: 'Игра' },
                    { id: 'GAME_MOD', name: 'Мод' },
                    { id: 'TELEGRAM_BOT', name: 'Телеграм-бот' },
                ]}
                validate={requiredMsg}
            />

            {/* Доп. поля */}
            <NumberInput
                source="externalId"
                label="Внешний ID"
                min={0}
                inputProps={{ onKeyDown: handleKeyDown, onPaste: handlePaste }}
            />

            <NumberInput
                source="quantityOfStudents"
                label="Количество участников"
                min={0}
                max={99}
                inputProps={{ maxLength: 2, onKeyDown: handleKeyDown, onPaste: handlePaste, onChange: handleChange2Digits }}
            />

            <TextInput
                source="captainName"
                label="Капитан"
                inputProps={{ maxLength: 64 }}
            />

            <TextInput
                source="track"
                label="Трек"
                inputProps={{ maxLength: 64 }}
            />

            <TextInput
                source="technologies"
                label="Технологии"
                multiline
                minRows={2}
                maxRows={6}
                inputProps={{ maxLength: 1024 }}
            />

            <BooleanInput
                source="full"
                label="Команда укомплектована"
            />
        </SimpleForm>
    );
};

export default ProjectAdminForm;
