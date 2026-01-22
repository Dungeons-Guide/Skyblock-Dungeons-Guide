# Project Structure

```mermaid
graph TD;
%%    Plugin --> Mod;
%%    Mod --> API & Loaders;
%%    Loaders --> Minecraft & API & SkyvelutterImpl[Skyvelutter Versioned Impl];
%%    SkyvelutterImpl --> SkyvelutterAPI & Minecraft
%%    API --> MinecraftAPI[Kyori Adventure]
%%    Mod --> SkyvelutterAPI[Skyvelutter API]
%%    
%%    Plugin[Version Specific Plugin];
%%    Mod[Dungeons Guide];
%%    Loaders[Version Specific Loaders];
%%    Minecraft[Versioned Minecraft];
%%    API[Generic Minecraft API - DG API];
%%    
%%    
%%    
    ModInterface --depends--> MinecraftAPI & SkyvelutterAPI & LoaderAPI;
    
    Mod --depends--> ModInterface;
    LoaderImpl --depends--> ModInterface  & MCAPIImpl & SkyvelutterImpl;
    MCAPIImpl --implements--> MinecraftAPI;
    MCAPIImpl --depends--> Minecraft;
    SkyvelutterImpl --implements--> SkyvelutterAPI;
    SkyvelutterImpl --depends--> Minecraft;
    
    Minecraft[Versioned Minecraft]
```
Arrows denote that `[from] depends on [to]`

LoaderAPI -- impl by -- Loader
MinecraftAPI -- impl by -- MC API impl
SkyvelutterAPI -- impl by -- Skyvelutter


Mod - Loader - Platform
