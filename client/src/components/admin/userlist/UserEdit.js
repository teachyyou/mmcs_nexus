import React from 'react';
import {
    Edit,
    required,
    SelectInput,
    SimpleForm,
    TextInput,
    Toolbar,
    SaveButton,
    DeleteButton,
    TopToolbar,
    useRedirect,
    useResourceContext,
} from 'react-admin';
import { Box, IconButton, Tooltip } from '@mui/material';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';

const EditActions = () => {
    const redirect = useRedirect();
    const resource = useResourceContext();

    return (
        <TopToolbar
            sx={{
                justifyContent: 'flex-start',
                pl: 0,
                gap: 1,
            }}
        >
            <Tooltip title="Назад">
                <IconButton
                    onClick={() => redirect('list', resource)}
                    size="small"
                    aria-label="Назад"
                >
                    <ArrowBackIosNewIcon fontSize="small" />
                </IconButton>
            </Tooltip>
        </TopToolbar>
    );
};

const UserEditToolbar = () => (
    <Toolbar sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <SaveButton />
        <Box sx={{ flex: 1 }} />
        <DeleteButton label="Заблокировать" />
    </Toolbar>
);

const UserEdit = (props) => {
    return (
        <Edit
            // title рендерится в AppBar, которого нет — отключаем
            title={false}
            actions={<EditActions />}
            {...props}
        >
            <SimpleForm toolbar={<UserEditToolbar />}>
                {/* READ-ONLY логин с аккуратным фоном для светлой/тёмной темы */}
                <TextInput
                    source="login"
                    name="login"
                    label="Логин"
                    variant="outlined"
                    InputProps={{ readOnly: true }}
                    sx={{
                        '& .MuiOutlinedInput-root': {
                            backgroundColor: (t) => t.palette.action.hover,
                        },
                        '& .MuiOutlinedInput-input': {
                            color: (t) => t.palette.text.primary,
                            opacity: 0.9,
                        },
                        '& .MuiOutlinedInput-notchedOutline': {
                            borderStyle: 'dashed',
                        },
                        '& .MuiOutlinedInput-root:hover .MuiOutlinedInput-notchedOutline': {
                            borderColor: (t) => t.palette.divider,
                        },
                        '& .MuiOutlinedInput-root.Mui-focused .MuiOutlinedInput-notchedOutline': {
                            borderColor: (t) => t.palette.primary.main,
                        },
                        '& .MuiOutlinedInput-input.MuiInputBase-input': {
                            cursor: 'text',
                        },
                    }}
                />

                <TextInput source="firstName" name="firstName" label="Имя" />
                <TextInput source="lastName"  name="lastName"  label="Фамилия" />
                <TextInput source="email"     name="email"     label="Почта" />

                <SelectInput
                    source="status"
                    name="status"
                    label="Статус"
                    choices={[
                        { id: 'VERIFIED',     name: 'Подтверждён' },
                        { id: 'NON_VERIFIED', name: 'Не подтверждён' },
                        { id: 'BLOCKED',      name: 'Заблокирован (WIP)' },
                    ]}
                    validate={required()}
                />

                <SelectInput
                    source="role"
                    name="role"
                    label="Роль"
                    choices={[
                        { id: 'ROLE_USER',  name: 'Пользователь' },
                        { id: 'ROLE_JURY',  name: 'Жюри' },
                        { id: 'ROLE_ADMIN', name: 'Администратор' },
                    ]}
                    validate={required()}
                />
            </SimpleForm>
        </Edit>
    );
};

export default UserEdit;
