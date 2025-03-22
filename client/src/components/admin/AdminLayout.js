import React from 'react';
import {AppBar, Layout} from 'react-admin';
import NavigationBar from '../home/NavigationBar';
import CustomAppBar from "./CustomAppBar"; // путь к вашему компоненту

// Создаём собственный AppBar, который включает вашу панель навигации
const MyAppBar = (props) => (
    <AppBar {...props}>
        <NavigationBar {...props} />
    </AppBar>
);

const AdminLayout = (props) => <Layout {...props} appBar={CustomAppBar} />;

export default AdminLayout;