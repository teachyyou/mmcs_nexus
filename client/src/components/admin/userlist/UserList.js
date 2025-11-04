import React from 'react';
import {
    List, Datagrid, TextField, FunctionField, BulkDeleteButton
} from 'react-admin';

const STATUS_LABELS = {
    VERIFIED: 'Подтверждён',
    NON_VERIFIED: 'Не подтверждён',
    BLOCKED: 'Заблокирован',
};
const ROLE_LABELS = {
    ROLE_USER: 'Пользователь',
    ROLE_ADMIN: 'Администратор',
    ROLE_JURY: 'Жюри',  
};

const UserList = (props) => (
    <List {...props} exporter={false} perPage={25}>
        <Datagrid bulkActionButtons={<BulkDeleteButton label="Заблокировать" />}>
            <TextField source="firstName" label="Имя" />
            <TextField source="lastName" label="Фамилия" />
            <TextField source="login" label="Логин" />
            <TextField source="email" label="Почта" />
            <FunctionField
                source="status"
                label="Статус"
                render={(r) => STATUS_LABELS[r.status] ?? r.status}
            />
            <FunctionField
                source="role"
                label="Роль"
                render={(r) => ROLE_LABELS[r.role] ?? r.role}
            />
        </Datagrid>
    </List>
);

export default UserList;
