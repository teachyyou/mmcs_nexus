import React from 'react';
import {
    BooleanField,
    CreateButton,
    Datagrid,
    DateField,
    FunctionField,
    List,
    TextField,
    TopToolbar,
} from 'react-admin';
import { Box } from '@mui/material';

const PostListActions = () => (
    <TopToolbar sx={{ pl: 0, gap: 1 }}>
        <CreateButton label="Создать" />
    </TopToolbar>
);

const PostList = (props) => (
    <List {...props} exporter={false} actions={<PostListActions />}>
        <Datagrid rowClick="edit">
            <TextField source="title" label="Заголовок" />
            <TextField source="previewText" label="Анонс" />
            <BooleanField source="published" label="Статус" />
            <DateField source="createdAt" label="Создан" showTime />
            <DateField source="publishedAt" label="Опубликован в" showTime />

            <FunctionField
                label="Баннер"
                render={(record) => record?.bannerUrl ? (
                    <Box
                        component="img"
                        src={record.bannerUrl}
                        alt={record.title}
                        sx={{
                            width: 96,
                            height: 54,
                            objectFit: 'cover',
                            borderRadius: 1,
                        }}
                    />
                ) : '-'}
            />
        </Datagrid>
    </List>
);

export default PostList;
