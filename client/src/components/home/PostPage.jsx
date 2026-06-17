import React, { useEffect, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    CircularProgress,
    Container,
    Paper,
    Typography,
} from '@mui/material';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import { useNavigate, useParams } from 'react-router-dom';

const PostPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const [post, setPost] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const loadPost = async () => {
            setLoading(true);
            setError(null);

            try {
                const response = await fetch(`/api/v1/public/posts/${id}`, {
                    credentials: 'include',
                });

                if (!response.ok) {
                    throw new Error('Пост не найден');
                }

                const data = await response.json();
                setPost(data);
            } catch (requestError) {
                setError(requestError.message || 'Ошибка загрузки поста');
            } finally {
                setLoading(false);
            }
        };

        loadPost();
    }, [id]);

    return (
        <Box sx={{ pt: 10, pb: 6, minHeight: '100vh' }}>
            <Container maxWidth="md">
                <Button
                    startIcon={<ArrowBackIosNewIcon />}
                    onClick={() => navigate(-1)}
                    sx={{ mb: 3 }}
                >
                    Назад
                </Button>

                {loading && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                        <CircularProgress />
                    </Box>
                )}

                {error && (
                    <Alert severity="error">
                        {error}
                    </Alert>
                )}

                {!loading && !error && post && (
                    <Paper sx={{ borderRadius: 3, overflow: 'hidden' }}>
                        {post.bannerUrl && (
                            <Box
                                component="img"
                                src={post.bannerUrl}
                                alt={post.title}
                                sx={{
                                    width: '100%',
                                    maxHeight: 420,
                                    objectFit: 'cover',
                                    display: 'block',
                                }}
                            />
                        )}

                        <Box sx={{ p: { xs: 3, sm: 4 } }}>
                            <Typography variant="h3" sx={{ fontWeight: 800, mb: 2 }}>
                                {post.title}
                            </Typography>

                            {post.publishedAt && (
                                <Typography color="text.secondary" sx={{ mb: 3 }}>
                                    Опубликовано: {new Date(post.publishedAt).toLocaleString('ru-RU')}
                                </Typography>
                            )}

                            <Box
                                sx={{
                                    '& img': { maxWidth: '100%' },
                                    '& h1, & h2, & h3': { mt: 3 },
                                    '& p': { lineHeight: 1.75 },
                                }}
                                dangerouslySetInnerHTML={{ __html: post.contentHtml }}
                            />

                            <Typography color="text.secondary" sx={{ mt: 4 }}>
                                Автор: {post.author || 'неизвестен'}
                            </Typography>

                        </Box>
                    </Paper>
                )}
            </Container>
        </Box>
    );
};

export default PostPage;