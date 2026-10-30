# Crazy Minecraft Mod (Fabric 1.20.1)

Мод для Minecraft 1.20.1 на платформе Fabric Loader.

## Требования
- Java 17+ (JDK)
- Fabric Loader 0.20.1+
- Fabric API

## Сборка и запуск
- Запуск клиентской среды разработки:
  ```bash
  ./gradlew runClient
  ```
- Сборка jar-файла мода:
  ```bash
  ./gradlew build
  ```
  Собранный мод появится в директории `build/libs/`.

- Подготовка проекта под IDEA:
  ```bash
  ./gradlew genSources
  ```
