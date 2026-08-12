import { Button } from '@mui/material'
import { BrowserRouter as Router, Navigate, Route, Routes , useLocation} from 'react-router-dom'
import { useContext, useEffect, useState } from 'react'
import { AuthContext } from 'react-oauth2-code-pkce'
import { useDispatch } from 'react-redux'
import { setCredentials } from './store/authSlice'
import ActivityForm from './components/ActivityForm'
import ActivityList from './components/ActivityList'
import {Box } from '@mui/material'
import ActivityDetail from './components/ActivityDetail'


const ActivitiesPage = () => {
  return (
    <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
      <p>hellooo</p>
      <ActivityForm onActivitiesAdded={() => window.location.reload()} />
      <ActivityList />
    </Box>
  )
}


function App() {
  const { token, tokenData, logIn } = useContext(AuthContext);
  const dispatch = useDispatch();
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    if (token) {
      dispatch(setCredentials({ token, user: tokenData }));
      setAuthReady(true);
    }
  }, [token, tokenData, dispatch]);

  return (
    <Router>
      {!token ? (
        <Button
          color="secondary"
          variant="contained"
          onClick={() => logIn()}
        >
          login
        </Button>
      ) : (
        <Box component="section" sx={{ p: 2, border: '1px dashed grey' }}>
          <Routes>
            <Route path="/activities" element={<ActivitiesPage />} />
            <Route path="/activity/:id" element={<ActivityDetail />} />
            <Route path="/" element={token ? <Navigate to="/activities" replace /> :<div>
              Welcome please login
            </div> } />
            
          </Routes>
        </Box>
      )}
    </Router>
  );
}


export default App
