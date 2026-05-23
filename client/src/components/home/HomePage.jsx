import React, { useEffect, useState } from 'react';
import {
    Alert,
    Box,
    Button,
    Card,
    CardContent,
    CardMedia,
    CircularProgress,
    Container,
    Divider,
    Stack,
    Typography,
} from '@mui/material';
import { Link } from 'react-router-dom';

const HomePage = () => {
    const [posts, setPosts] = useState([]);
    const [total, setTotal] = useState(0);
    const [offset, setOffset] = useState(0);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [error, setError] = useState(null);

    const limit = 10;

    const loadPosts = async (nextOffset = 0, append = false) => {
        append ? setLoadingMore(true) : setLoading(true);
        setError(null);

        try {
            const response = await fetch(
                `/api/v1/public/posts?limit=${limit}&offset=${nextOffset}&sort=publishedAt&order=desc`,
                { credentials: 'include' }
            );

            if (!response.ok) {
                throw new Error('Не удалось загрузить посты');
            }

            const data = await response.json();
            const items = data.data || data.content || [];

            setPosts((current) => append ? [...current, ...items] : items);
            setTotal(data.total ?? data.totalElements ?? items.length);
            setOffset(nextOffset);
        } catch (requestError) {
            setError(requestError.message || 'Ошибка загрузки постов');
        } finally {
            append ? setLoadingMore(false) : setLoading(false);
        }
    };

    useEffect(() => {
        loadPosts();
    }, []);

    const hasMore = posts.length < total;

    return (
        <Box sx={{ pt: 10, pb: 6, minHeight: '100vh' }}>
            <Container maxWidth="md">
                {loading && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
                        <CircularProgress />
                    </Box>
                )}

                {error && (
                    <Alert severity="error" sx={{ mb: 3 }}>
                        {error}
                    </Alert>
                )}

                {!loading && !error && posts.length === 0 && (
                    <Alert severity="info">
                        Пока нет опубликованных постов
                    </Alert>
                )}

                <Stack spacing={0}>
                    {posts.map((post, index) => (
                        <React.Fragment key={post.id}>
                            {index > 0 && <Divider sx={{ my: 4 }} />}

                            <Card
                                sx={{
                                    borderRadius: 3,
                                    overflow: 'hidden',
                                }}
                            >
                                {post.bannerUrl && (
                                    <CardMedia
                                        component="img"
                                        image={post.bannerUrl}
                                        alt={post.title}
                                        sx={{
                                            height: { xs: 220, sm: 320 },
                                            objectFit: 'cover',
                                        }}
                                    />
                                )}

                                <CardContent sx={{ p: 3 }}>
                                    <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
                                        {post.title}
                                    </Typography>

                                    {post.previewText && (
                                        <Typography color="text.secondary" sx={{ whiteSpace: 'pre-line', mb: 2 }}>
                                            {post.previewText}
                                        </Typography>
                                    )}

                                    <Box
                                        sx={{
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'space-between',
                                            gap: 2,
                                            flexWrap: 'wrap',
                                            mt: 2,
                                        }}
                                    >
                                        <Box>
                                            <Typography variant="caption" color="text.secondary">
                                                Автор: {post.author || 'неизвестен'}
                                            </Typography>

                                            {post.publishedAt && (
                                                <Typography variant="caption" color="text.secondary" sx={{ ml: 2 }}>
                                                    {new Date(post.publishedAt).toLocaleString('ru-RU')}
                                                </Typography>
                                            )}
                                        </Box>

                                        <Button
                                            component={Link}
                                            to={`/posts/${post.id}`}
                                            variant="text"
                                        >
                                            Читать
                                        </Button>
                                    </Box>
                                </CardContent>
                            </Card>
                        </React.Fragment>
                    ))}
                </Stack>

                {hasMore && (
                    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                        <Button
                            variant="contained"
                            disabled={loadingMore}
                            onClick={() => loadPosts(offset + limit, true)}
                        >
                            {loadingMore ? 'Загрузка…' : 'Показать ещё'}
                        </Button>
                    </Box>
                )}
            </Container>
        </Box>
    );
};

export default HomePage;