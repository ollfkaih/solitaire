import { Box, Container, Typography } from "@mui/material";
import { green } from "@mui/material/colors";
import "./App.css";

function App() {
  return (
    <Container sx={{ backgroundColor: green[700], padding: 0, margin: 0 }}>
      <Box sx={{ my: 4 }}>
        <Typography variant="h4" component="h1" gutterBottom>
          Create React App example with TypeScript
        </Typography>
      </Box>
    </Container>
  );
}

export default App;
