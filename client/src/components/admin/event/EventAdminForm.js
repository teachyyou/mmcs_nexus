// EventAdminForm.js
import React from 'react';
import {
    NumberInput,
    required,
    SelectInput,
    SimpleForm,
    TextInput,
    useNotify,
    useRecordContext,
    useRedirect,
    Toolbar,
    SaveButton,
    DeleteButton,
} from 'react-admin';
import { Box } from '@mui/material';

const EventFormToolbar = () => {
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

const EventAdminForm = (props) => {
    const { requestMethod } = props;
    const notify = useNotify();
    const redirect = useRedirect();
    const record = useRecordContext();

    const handleKeyDown = (e) => {
        if (e.key === '-' || e.key === 'e' || e.key === 'E') e.preventDefault();
    };
    const handlePaste = (e) => {
        const paste = e.clipboardData.getData('text');
        if (/[-eE]/.test(paste)) e.preventDefault();
    };
    const handleChange = (e) => {
        const orig = e.target.value;
        const cleaned = orig.replace(/[eE-]/g, '');
        if (orig !== cleaned) e.target.value = cleaned;
        else if (cleaned.length > 2) e.target.value = cleaned.slice(0, 2);
    };

    const year = new Date().getFullYear();
    const requiredMsg = required('Обязательное поле');

    const handleSubmit = async (data) => {
        const { id, ...dto } = data;
        const params = record?.id ? { id: record.id, data: dto } : { data: dto };

        requestMethod('events', params)
            .then(() => {
                notify('Сохранено успешно');
                redirect('list', 'events');
            })
            .catch((error) => {
                if (error.status === 409) {
                    notify('Событие с такими параметрами уже существует', { type: 'warning' });
                } else {
                    notify('Неизвестная ошибка', { type: 'error' });
                }
            });
    };

    return (
        <SimpleForm onSubmit={handleSubmit} toolbar={<EventFormToolbar />}
                    sx={{
                        maxWidth: 900,
                        width: '100%',
                        '& .MuiFormControl-root': {
                            width: '100%',
                        },
                        '& .RaInput-root': {
                            width: '100%',
                        },
                        gap: 2,
                    }}
        >
            <TextInput
                source="name"
                label="Название"
                validate={requiredMsg}
                inputProps={{ maxLength: 32 }}
            />
            <NumberInput
                source="year"
                label="Год"
                min={year - 5}
                max={year + 5}
                defaultValue={year}
                validate={requiredMsg}
                inputProps={{ maxLength: 4 }}
            />
            <SelectInput
                source="eventType"
                label="Тип события"
                choices={[
                    { id: 'IDEA', name: 'Защита идеи' },
                    { id: 'ZERO_VERSION', name: 'Защита нулевой версии' },
                    { id: 'PRE_RELEASE', name: 'Предзащита' },
                    { id: 'RELEASE', name: 'Итоговая защита' },
                ]}
                validate={requiredMsg}
            />
            <NumberInput
                source="maxBuildPoints"
                label="Максимальная оценка за билд"
                validate={requiredMsg}
                min={0}
                max={35}
                inputProps={{
                    onKeyDown: handleKeyDown,
                    onPaste: handlePaste,
                    onChange: handleChange,
                    maxLength: 2,
                }}
            />
            <NumberInput
                source="maxPresPoints"
                label="Максимальная оценка за презентацию"
                validate={requiredMsg}
                min={0}
                max={15}
                inputProps={{
                    onKeyDown: handleKeyDown,
                    onPaste: handlePaste,
                    onChange: handleChange,
                    maxLength: 2,
                }}
            />
        </SimpleForm>
    );
};

export default EventAdminForm;
