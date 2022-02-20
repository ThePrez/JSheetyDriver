# Jesse's Sheety Driver (JSheetyDriver)
A JDBC driver for interacting with CSV files and Excel Spreadsheets.

This is still in bringup and not suitable for production use. Doc and testing forthcoming.

# Features

# Getting Started

# Schema and Table Geometry

```mermaid
graph TD
    A[How to reference opened spreadsheet?] --> B{Excel or CSV?};
    B -- Excel --> C[table name = sheet name];
    B -- CSV --> D[table name = 'CSV'];
    D --> E[Schema = file name]
    C --> E[Schema = file name]
```

# Special Commands
