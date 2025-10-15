import React from 'react';
import { Edit, required, SelectInput, SimpleForm, TextInput } from 'react-admin';

const UserEdit = (props) => {
    return (
        <Edit title="Edit User" {...props}>
            <SimpleForm>

                {/* READ-ONLY логин с темизуемым фоном */}
                <TextInput
                    source="login"
                    name="login"
                    label="Логин"
                    variant="outlined"
                    InputProps={{ readOnly: true }}
                    sx={{
                        // общий фон поля – использует палитру темы
                        '& .MuiOutlinedInput-root': {
                            backgroundColor: (t) => t.palette.action.hover,
                        },
                        // текст делаем чуть приглушённым, но читаемым в обеих темах
                        '& .MuiOutlinedInput-input': {
                            color: (t) => t.palette.text.primary,
                            opacity: 0.9,
                        },
                        // пунктирная рамка, чтобы визуально показать «нельзя редактировать»
                        '& .MuiOutlinedInput-notchedOutline': {
                            borderStyle: 'dashed',
                        },
                        // на ховер/фокус цвет рамки из темы
                        '& .MuiOutlinedInput-root:hover .MuiOutlinedInput-notchedOutline': {
                            borderColor: (t) => t.palette.divider,
                        },
                        '& .MuiOutlinedInput-root.Mui-focused .MuiOutlinedInput-notchedOutline': {
                            borderColor: (t) => t.palette.primary.main,
                        },
                        // курсор «текст», но без изменения значения
                        '& .MuiOutlinedInput-input.MuiInputBase-input': {
                            cursor: 'text',
                        },
                    }}
                />

                <TextInput source="firstName" name="firstName" label="Имя" />
                <TextInput source="lastName"  name="lastName"  label="Фамилия" />
                <TextInput source="email"  name="email"  label="Почта" />

                <SelectInput
                    source="status"
                    name="status"
                    label="Статус"
                    choices={[
                        { id: 'VERIFIED', name: 'Подтверждён' },
                        { id: 'NON_VERIFIED', name: 'Не подтверждён' },
                        { id: 'BLOCKED', name: 'Заблокирован (WIP)' },
                    ]}
                    validate={required()}
                />

                <SelectInput
                    source="role"
                    name="role"
                    label="Роль"
                    choices={[
                        { id: 'ROLE_USER',  name: 'Пользовательь' },
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
