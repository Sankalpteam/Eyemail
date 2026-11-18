import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js";

export const firebaseConfig = {
  apiKey: "AIzaSyD8NEGRRWsQSrnwHHDOn3uzBUlKQWUHiuc",
  authDomain: "eyemail-c3398.firebaseapp.com",
  databaseURL: "https://eyemail-c3398-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "eyemail-c3398",
  storageBucket: "eyemail-c3398.firebasestorage.app",
  messagingSenderId: "722592118925",
  appId: "1:722592118925:web:aecc6273e6b244570ecac4",
  measurementId: "G-LBVQ51D76H"
};

export const BACKEND_URL = "https://eyemail-backend.onrender.com";
export const DOMAIN_NAME = "eyemail.in";

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const db = getFirestore(app);
