const express = require("express");
const mongoose = require("mongoose");
const bodyParser = require("body-parser");

// Configuración del servidor y MongoDB
const app = express();
const PORT = 5000; // Puerto de la API
const MONGO_URI = "mongodb://localhost:27017/networkInfoDB"; // URI de MongoDB

// Middleware
app.use(bodyParser.json());

// Conectar a MongoDB
mongoose
  .connect(MONGO_URI, { useNewUrlParser: true, useUnifiedTopology: true })
  .then(() => console.log("Conectado a MongoDB"))
  .catch((error) => console.error("Error al conectar a MongoDB:", error));

// Esquema y Modelo de MongoDB
const networkInfoSchema = new mongoose.Schema({
  networks: {
    WiFi: {
      SSID: String,
      BSSID: String,
      ipAddress: String,
      linkSpeedMbps: Number,
      frequencyMHz: Number,
      signalStrengthRSSI: Number,
    },
    MobileData: {
      operatorName: String,
      networkType: String,
      signalStrengthRSSI: Number,
      isRoaming: Boolean,
    },
  },
  availableWiFiNetworks: [
    {
      SSID: String,
      BSSID: String,
      frequency: Number,
      signalLevel: Number,
    },
  ],
  timestamp: { type: Date, default: Date.now },
});

const NetworkInfo = mongoose.model("NetworkInfo", networkInfoSchema);

// Rutas de la API

// Guardar información de red
app.post("/api/networkinfo", async (req, res) => {
  try {
    const networkInfo = new NetworkInfo(req.body);
    await networkInfo.save();
    res.status(201).json({ message: "Información de red guardada con éxito" });
  } catch (error) {
    res.status(500).json({
      message: "Error al guardar la información de red",
      error: error.message,
    });
  }
});

// Obtener toda la información de red
app.get("/api/networkinfo", async (req, res) => {
  try {
    const data = await NetworkInfo.find();
    res.status(200).json(data);
  } catch (error) {
    res.status(500).json({
      message: "Error al obtener la información de red",
      error: error.message,
    });
  }
});

// Iniciar el servidor
app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
