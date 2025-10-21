
# 🐣 Smart Incubator Management App - by Rekibur Uddin

A professional **Android application** for managing the complete lifecycle of incubators – from **inventory management** of machine parts to **building incubators**, tracking **sales history**, and generating **business reports**.  

Built with **Kotlin** and powered by **Firebase**, this app ensures smooth, reliable, and real-time management for businesses.

---

## 📽️ Demo

<p align="center">
  <img src="https://github.com/user-attachments/assets/b1d64e67-0735-4f58-9fa2-427c2462c5c8" width="250" />
  <img src="https://github.com/user-attachments/assets/28ff78d0-2ab7-441d-8586-6be07e7f953d" width="250" />
  <img src="https://github.com/user-attachments/assets/efc9cbd7-2d86-4800-967a-3e60bd6fb9c1" width="250" />
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/0082135c-c903-4c59-9505-7e70100c01be" width="250" />
  <img src="https://github.com/user-attachments/assets/7fabd21b-72d2-42af-8d9f-a2c6b70a4fe0" width="250" />
</p>

---

## ✨ Features

### 🔧 Inventory Management

* Add and manage **machine parts** required for building incubators.
* **Stock Level Indicators**:

  * 🟢 **Green** – Stock available (≥10 items)
  * 🟡 **Yellow** – Low stock (<10 items)
  * 🔴 **Red** – Critical stock (<5 items)
* Real-time monitoring to prevent shortages.

### 🏗️ Incubator Lifecycle

* Record **built incubators** with detailed specifications.
* Track **completed incubators** in inventory.
* Add **sold incubators** with buyer details, cost, and selling price.
* Maintain full history for each incubator.

### 📊 Business Reports

* Generate reports by **month, year, or custom date range**.
* Insights include:

  * Total incubators built
  * Total incubators sold
  * Profit & cost breakdown
  * Sales history
* Helps in **business decision-making** with clear data.

### ☁️ Firebase Integration

* Powered by **Firebase Firestore** for data storage and synchronization.
* Ensures **real-time updates** and scalability.

---

## 🛠️ Tech Stack

* **Language:** Kotlin
* **Architecture:** MVVM + Clean Architecture
* **UI:** XML + Material Design Components
* **Database:** Firebase Firestore + Room (for local cache)
* **Networking:** Retrofit, OkHttp
* **Dependency Injection:** Hilt
* **Tools:** Android Studio, Git, GitHub

---

## 📂 Project Structure

```
SmartIncubatorManagement/
 ┣ app/
 ┃ ┣ java/
 ┃ ┃ ┣ com.app.smartincubator/
 ┃ ┃ ┃ ┣ ui/        # Activities & Fragments
 ┃ ┃ ┃ ┣ data/      # Firebase & Room Repositories
 ┃ ┃ ┃ ┣ model/     # Data Models
 ┃ ┃ ┃ ┣ viewmodel/ # MVVM ViewModels
 ┃ ┣ res/           # Layouts, Drawables, Values
 ┣ build.gradle
 ┗ README.md
```

---

## ⚡ Getting Started

### Prerequisites

* Android Studio (latest version)
* Firebase Project with `google-services.json`

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/your-username/Smart-Incubator-Managment.git
   ```
2. Open the project in **Android Studio**.
3. Connect to **Firebase** by adding your `google-services.json`.
4. Sync Gradle and run on an emulator or device.

---

## 👨‍💻 Author

**Rekibur Uddin**

* 📧 Email: [rekibdev@gmail.com](mailto:rekibdev@gmail.com)
* 🌐 Portfolio: [rekiburuddin.blogspot.com](https://rekiburuddin.blogspot.com)

---

## 📜 License

```
Copyright 2025 Rekibur Uddin

Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at  

    http://www.apache.org/licenses/LICENSE-2.0  

Unless required by applicable law or agreed to in writing, software  
distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.
```

---

