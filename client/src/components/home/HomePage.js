import React, {useEffect, useState} from 'react';
import NavigationBar from './NavigationBar';
import {StyledEngineProvider} from "@mui/material";

const HomePage = ({ isAuthenticated, setIsAuthenticated }) => {

    const [isLoading, setIsLoading] = useState(true);
    const [login, setLogin] = useState("default_user");
    const [name, setName] = useState("Unknown User");
    const [avatar_url, setAvatarUrl] = useState("default_url");

    useEffect(() => {
        if (isAuthenticated) {
            fetch('http://localhost:8080/api/v1/auth/user', {
                credentials: 'include'
            })
                .then(
                    response => response.json())
                .then(data => {
                    setName(data.name);
                    setLogin(data.login);
                    setAvatarUrl(data.avatar_url);
                    setIsLoading(false);
                })
        }
        else {
            setName("Guest");
            setAvatarUrl(null)
            setIsLoading(false)
        }
    }, [isAuthenticated]);

    if (!isLoading) return (
        <div className="App">
            <header className="App-header">
                <StyledEngineProvider injectFirst>
                    <NavigationBar className='navigation-bar' isAuthenticated={isAuthenticated}
                                   setIsAuthenticated={setIsAuthenticated}/>
                </StyledEngineProvider>
            </header>
            Welcome, {name}!
            <img src={avatar_url} alt = ""/>
        </div>
    );
};

export default HomePage;
