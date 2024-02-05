package com.oxuegen.petrinet;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    private Circle ordersQueue;
    private Circle assemblyStage;
    private Circle paintingStage;
    private Circle testingStage;
    private Circle finishedProducts;

    private Circle parallelPaintingStage;

    Circle parallelTestingStage;
    private Line arrow1;
    private Line arrowToAssembly;
    private Line arrowToPainting;
    private Line arrowToTesting;
    private Line arrowFromAssembly;
    private Line arrowFromPainting;
    private Line arrowFromTesting;
    private Line arrowToFinished;

    private Line arrowToParallelPainting;

    private Line arrowToParallelTesting;
    private Text[] resourceTexts;
    private Text messageText;
    private int currentStageIndex = 0; // Индекс текущего этапа

    private boolean isProcessing = false;
    private Color[] originalColors;
    private double[] originalRadius;
    private int availableWorkers = 1; // Количество доступных рабочих
    private int assemblyTime = 2; // Время на сборку машины (в условных единицах времени)
    private int paintingTime = 3; // Время на покраску машины (в условных единицах времени)
    private int testingTime = 2; // Время на тестирование машины (в условных единицах времени)

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();

        // Создаем места (Places)
        ordersQueue = createPlace(100, 300, "Orders Queue");
        assemblyStage = createPlace(400, 200, "Assembly Stage");
        paintingStage = createPlace(700, 200, "Painting Stage");
        testingStage = createPlace(1000, 200, "Testing Stage");
        finishedProducts = createPlace(1300, 300, "Finished Products");
        // Добавляем параллельные стадии
        parallelPaintingStage = createPlace(700, 400, "Parallel Painting Stage");
        parallelTestingStage = createPlace(1000, 400, "Parallel Testing Stage");
        // Создаем переходы (Transitions)
        Circle startAssembly = createTransition(250, 300, "Start Assembly");
        Circle startPainting = createTransition(550, 200, "Start Painting");
        Circle startTesting = createTransition(850, 200, "Start Testing");
        Circle endProduction = createTransition(1150, 300, "End Production");
        // Создаем переходы для параллельных стадий
        Circle parallelPaintingTransition = createTransition(700, 300, "Parallel Painting Transition");
        Circle parallelTestingTransition = createTransition(1000, 300, "Parallel Testing Transition");

        // Сохраняем изначальные цвета и радиусы
        originalColors = new Color[]{
                (Color) ordersQueue.getFill(),
                (Color) assemblyStage.getFill(),
                (Color) paintingStage.getFill(),
                (Color) testingStage.getFill(),
                (Color) finishedProducts.getFill(),
                (Color) parallelPaintingStage.getFill(),
                (Color) parallelTestingStage.getFill()
        };

        originalRadius = new double[]{
                ordersQueue.getRadius(),
                assemblyStage.getRadius(),
                paintingStage.getRadius(),
                testingStage.getRadius(),
                finishedProducts.getRadius(),
                parallelPaintingStage.getRadius(),
                parallelTestingStage.getRadius()
        };

        // Создаем стрелки (Arcs)
        arrow1 = createArrow(ordersQueue, startAssembly);
        arrowToAssembly = createArrow(startAssembly, assemblyStage);
        arrowToPainting = createArrow(startPainting, paintingStage);
        arrowToTesting = createArrow(startTesting, testingStage);
        arrowFromAssembly = createArrow(assemblyStage, startPainting);
        arrowFromPainting = createArrow(paintingStage, startTesting);
        arrowFromTesting = createArrow(testingStage, endProduction);
        arrowToFinished = createArrow(endProduction, finishedProducts);

        // Добавляем стрелки к переходам
        arrowToParallelPainting = createArrow(startPainting, parallelPaintingTransition);
        arrowToParallelTesting = createArrow(startTesting, parallelTestingTransition);

        // Добавляем подписи к переходам
        Text parallelPaintingText = createText(700, 290, "Parallel Painting");
        Text parallelTestingText = createText(1000, 290, "Parallel Testing");


        // Добавляем элементы на панель
        root.getChildren().addAll(ordersQueue, assemblyStage, paintingStage,
                testingStage, finishedProducts, parallelPaintingStage, parallelTestingStage);
        root.getChildren().addAll(startAssembly, startPainting, startTesting, parallelPaintingTransition, parallelTestingTransition, endProduction);
        root.getChildren().addAll(arrow1, arrowToAssembly, arrowToPainting, arrowToTesting,
                arrowFromAssembly, arrowFromPainting, arrowFromTesting, arrowToFinished
                , arrowToParallelPainting, arrowToParallelTesting);

        // Добавляем подписи
        root.getChildren().addAll(getTexts(ordersQueue, assemblyStage, paintingStage, testingStage, finishedProducts));
        root.getChildren().addAll(getTexts(startAssembly, startPainting, startTesting, endProduction));

        // Добавляем текстовые поля для отображения информации о ресурсах
        resourceTexts = new Text[3];
        resourceTexts[0] = createText(1200, 20, "Available Workers: " + availableWorkers);
        resourceTexts[1] = createText(1200, 40, "Assembly Time: " + assemblyTime + " units");
        resourceTexts[2] = createText(1200, 60, "Painting Time: " + paintingTime + " units");

        // Добавляем текстовое поле для вывода сообщений
        messageText = createText(400, 400, "");
        root.getChildren().addAll(resourceTexts[0], resourceTexts[1], resourceTexts[2], messageText);

        // Добавляем поля для пользовательского ввода
        TextField workersInput = createTextField(1200, 120, "Enter Workers:");
        TextField assemblyTimeInput = createTextField(1200, 150, "Enter Assembly Time:");
        TextField paintingTimeInput = createTextField(1200, 180, "Enter Painting Time:");

        // Комбо-бокс для выбора времени тестирования
        Label testingTimeLabel = new Label("Select Testing Time:");
        ComboBox<Integer> testingTimeComboBox = createComboBox(1200, 210, new Integer[]{1, 2, 3, 4, 5});
        testingTimeComboBox.setValue(testingTime);

        // Кнопка для симуляции изменения состояний
        Button simulateButton = new Button("Simulate Production");
        simulateButton.setLayoutX(1200);
        simulateButton.setLayoutY(250);
        simulateButton.setOnAction(event -> simulateProduction(workersInput, assemblyTimeInput, paintingTimeInput, testingTimeComboBox));

        // Добавляем все элементы на панель
        root.getChildren().addAll(workersInput, assemblyTimeInput, paintingTimeInput, testingTimeLabel, testingTimeComboBox, simulateButton);

        Scene scene = new Scene(root, 1400, 500);
        primaryStage.setTitle("User Input Production Process Petri Net");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private TextField createTextField(double x, double y, String promptText) {
        TextField textField = new TextField();
        textField.setLayoutX(x);
        textField.setLayoutY(y);
        textField.setPromptText(promptText);
        return textField;
    }

    private ComboBox<Integer> createComboBox(double x, double y, Integer[] values) {
        ComboBox<Integer> comboBox = new ComboBox<>();
        comboBox.setLayoutX(x);
        comboBox.setLayoutY(y);
        comboBox.getItems().addAll(values);
        return comboBox;
    }
    private Text[] getTexts(Circle... circles) {
        Text[] texts = new Text[circles.length];
        for (int i = 0; i < circles.length; i++) {
            texts[i] = createText(circles[i].getCenterX() - 40, circles[i].getCenterY() + 60, circles[i].getId());
        }
        return texts;
    }
    private Text createText(double x, double y, String label) {
        Text text = new Text(x, y, label);
        return text;
    }
    private Circle createPlace(double x, double y, String label) {
        Circle place = new Circle(x, y, 40, Color.LIGHTBLUE);
        place.setStroke(Color.BLACK);
        place.setId(label);
        return place;
    }
    private void resetArrows() {
        arrow1.setStroke(Color.LIGHTGRAY);
        arrowToAssembly.setStroke(Color.LIGHTGRAY);
        arrowToPainting.setStroke(Color.LIGHTGRAY);
        arrowToTesting.setStroke(Color.LIGHTGRAY);
        arrowFromAssembly.setStroke(Color.LIGHTGRAY);
        arrowFromPainting.setStroke(Color.LIGHTGRAY);
        arrowFromTesting.setStroke(Color.LIGHTGRAY);
        arrowToFinished.setStroke(Color.LIGHTGRAY);
    }
    private Circle createTransition(double x, double y, String label) {
        Circle transition = new Circle(x, y, 30, Color.LIGHTGREEN);
        transition.setStroke(Color.BLACK);
        transition.setId(label);
        return transition;
    }

    private Line createArrow(Circle from, Circle to) {
        Line arrow = new Line();
        arrow.setStartX(from.getCenterX());
        arrow.setStartY(from.getCenterY());
        arrow.setEndX(to.getCenterX());
        arrow.setEndY(to.getCenterY());
        return arrow;
    }

    private void resetStageAppearance() {
        // Восстанавливаем изначальные цвета и радиусы кругов
        ordersQueue.setFill(originalColors[0]);
        assemblyStage.setFill(originalColors[1]);
        paintingStage.setFill(originalColors[2]);
        testingStage.setFill(originalColors[3]);
        finishedProducts.setFill(originalColors[4]);
        parallelPaintingStage.setFill(originalColors[5]);
        parallelTestingStage.setFill(originalColors[6]);

        ordersQueue.setRadius(originalRadius[0]);
        assemblyStage.setRadius(originalRadius[1]);
        paintingStage.setRadius(originalRadius[2]);
        testingStage.setRadius(originalRadius[3]);
        finishedProducts.setRadius(originalRadius[4]);
        parallelPaintingStage.setRadius(originalRadius[5]);
        parallelTestingStage.setRadius(originalRadius[6]);
    }

    private void simulateProduction(TextField workersInput, TextField assemblyTimeInput, TextField paintingTimeInput, ComboBox<Integer> testingTimeComboBox) {
        try {
            availableWorkers = Integer.parseInt(workersInput.getText());
            assemblyTime = Integer.parseInt(assemblyTimeInput.getText());
            paintingTime = Integer.parseInt(paintingTimeInput.getText());
            testingTime = testingTimeComboBox.getValue();

            if (availableWorkers > 0) {
                currentStageIndex++;

                switch (currentStageIndex) {
                    case 1:
                        simulateStage(assemblyStage, arrowToAssembly, arrowFromAssembly, arrowToPainting, assemblyTime);
                        break;
                    case 2:
                        simulateStage(paintingStage, arrowToPainting, arrowFromPainting, arrowToTesting, paintingTime);
                        break;
                    case 3:
                        simulateStage(testingStage, arrowToTesting, arrowFromTesting, arrowToFinished, testingTime);
                        break;
                    case 4:
                        finishedProducts.setRadius(60);
                        finishedProducts.setFill(Color.LIGHTGREEN);
                        resetArrows();
                        resetStageAppearance();
                        currentStageIndex = 0;
                        break;
                }

                resourceTexts[0].setText("Available Workers: " + availableWorkers);
                messageText.setText("");
            } else {
                messageText.setText("No available workers!");
            }
        } catch (NumberFormatException e) {
            messageText.setText("Please enter valid numeric values.");
        }
    }

    private void simulateStage(Circle currentStage, Line arrowTo, Line arrowFrom, Line resetArrow, int time) {
        // Переходим к следующему этапу
        currentStage.setRadius(50);
        currentStage.setFill(Color.LIGHTGREEN);
        arrowTo.setStroke(Color.BLACK);
        arrowFrom.setStroke(Color.LIGHTGRAY);
        resetArrow.setStroke(Color.LIGHTGRAY);

        // Уменьшаем количество доступных рабочих
        availableWorkers--;

        // Запускаем таймер для симуляции времени
        new Thread(() -> {
            try {
                Thread.sleep(time * 1000); // Имитация времени в секундах
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Возвращаем стрелки в исходное состояние
            arrowTo.setStroke(Color.LIGHTGRAY);
            arrowFrom.setStroke(Color.LIGHTGRAY);
            resetArrow.setStroke(Color.BLACK);

            // Увеличиваем количество доступных рабочих
            availableWorkers++;
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}