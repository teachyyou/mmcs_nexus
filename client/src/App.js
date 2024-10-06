import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import LoginPage from './components/login/LoginPage';
import HomePage from './components/home/HomePage';
import './App.css';
import UpdateProfilePage from "./components/login/UpdateProfilePage";
import ProtectedAuthenticationRoutes from './components/routes/ProtectedAuthenticationRoutes';
import OnlyAuthenticatedRoutes from "./components/routes/OnlyAuthenticatedRoutes";
import AuthenticatedAndVerifiedRoutes from "./components/routes/AuthenticatedAndVerifiedRoutes";
import UserList from "./components/admin/userlist/UserList";
import UserEdit from "./components/admin/userlist/UserEdit";
import {Admin, Resource} from "react-admin";
import springBootRestProvider from "./components/admin/restProviders/springBootRestProvider";
import ProjectList from "./components/admin/projectlist/ProjectList";
import ProjectEdit from "./components/admin/projectlist/ProjectEdit";
import ProjectCreate from "./components/admin/projectlist/ProjectCreate";
import EventList from "./components/admin/eventList/EventList";
import EventEdit from "./components/admin/eventList/EventEdit";
import EventCreate from "./components/admin/eventList/EventCreate";
import ProjectManagment from "./components/admin/ProjectManagment";


const dataProvider = springBootRestProvider('http://localhost:8080/api/v1/admin');

function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/auth/status', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
                setIsLoading(false);
            });
    }, []);

    if (!isLoading) return (
        <Router>
            <Routes>
                {/*can get there only when not-authenticated OR updated user data*/}
                <Route element={<ProtectedAuthenticationRoutes isAuthenticated={isAuthenticated}/>}>
                    <Route path="/" element={<HomePage isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />} />
                    <Route path="/login" element={<LoginPage />} />
                </Route>

                {/*can get here only when authenticated and updated user data*/}
                <Route element={<AuthenticatedAndVerifiedRoutes isAuthenticated={isAuthenticated}/>}>

                </Route>

                {/*can get here only when authenticated*/}
                <Route element={<OnlyAuthenticatedRoutes isAuthenticated={isAuthenticated}/>}>
                    <Route path="/update-profile" element={<UpdateProfilePage />} />
                    <Route path = "admin/*" element={
                        <Admin dataProvider={dataProvider} basename="/admin">
                            <Resource name="users" list={UserList} edit ={UserEdit}  />
                            <Resource name="projects" list={ProjectList} edit ={ProjectEdit} create={ProjectCreate} />
                            <Resource name="events" list={EventList} edit ={EventEdit} create={EventCreate} />
                            <Resource name="pjm" list={ProjectManagment} edit ={ProjectManagment} create={ProjectManagment}/>
                        </Admin>
                    } />
                </Route>
            </Routes>
        </Router>
    );

}

export default App;
