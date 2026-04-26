import React from 'react';
import {
    Create,
    Edit,
    TopToolbar,
    useDataProvider,
    useRedirect,
    useResourceContext,
} from 'react-admin';
import { IconButton, Tooltip } from '@mui/material';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import PostAdminForm from './PostAdminForm';

const PostActions = () => {
    const redirect = useRedirect();
    const resource = useResourceContext();

    return (
        <TopToolbar sx={{ justifyContent: 'flex-start', pl: 0, gap: 1 }}>
            <Tooltip title="Назад">
                <IconButton onClick={() => redirect('list', resource)} size="small" aria-label="Назад">
                    <ArrowBackIosNewIcon fontSize="small" />
                </IconButton>
            </Tooltip>
        </TopToolbar>
    );
};

export const PostCreate = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Create title="Добавить пост" actions={<PostActions />} redirect="list" {...props}>
            <PostAdminForm requestMethod={dataProvider.create} mode="create" />
        </Create>
    );
};

export const PostEdit = (props) => {
    const dataProvider = useDataProvider();

    return (
        <Edit
            title="Редактировать пост"
            actions={<PostActions />}
            mutationMode="pessimistic"
            redirect="list"
            {...props}
        >
            <PostAdminForm requestMethod={dataProvider.update} mode="edit" />
        </Edit>
    );
};