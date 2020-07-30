package com.vanishedmc.cms18garageorder;

import java.awt.*;
import java.io.File;
import javax.swing.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;

public class Main {

    // Applications height
    private static final int HEIGHT = 500;

    // Main jframe
    private JFrame frame;
    // All loaded profiles
    private List<File> profiles;
    // All the files in the current active profile
    private List<File> garageFiles;
    // The current selected cars
    private int car1 = -1, car2 = -1;
    private JToggleButton b1 = null, b2 = null;
    // The current active profile
    private File currentProfile = null;

    // The ranges for each garage floor
    // https://steamcommunity.com/sharedfiles/filedetails/?id=1985395208
    private final int[][] ranges = {
        {0, 11},
        {12, 23},
        {24, 35},
        {36, 47},
        {48, 59},
        {60, 71},
        {72, 83},
        {84, 95},
        {96, 107},
        {108, 119},
        {120, 131},
        {132, 143},
        {144, 155},
        {156, 167},
    };

    public static void main(String... args) {
        new Main();
    }

    private Main () {
        // Init new array
        profiles = new ArrayList<>();

        // Get the game data folder, or exit application if it can not be found
        File gameSaveFolder = new File("\\" + System.getenv("USERPROFILE") + "\\appdata\\locallow\\Red Dot Games\\Car Mechanic Simulator 2018\\");
        if(!gameSaveFolder.exists() || !gameSaveFolder.isDirectory()) {
            System.exit(-1);
        }

        // Load all the profiles into a list
        for (File file : gameSaveFolder.listFiles()) {
            if (file.getName().startsWith("profile") && file.isDirectory()) {
               profiles.add(file);
            }
        }

        // Set up JFrame
        frame = new JFrame("Car Mechanic Simulator 2018 garage order manager");
        Dimension size = new Dimension(200, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(size);
        frame.setMinimumSize(size);
        frame.setMaximumSize(size);
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

        loadProfileSelector();
    }

    private void loadProfileSelector() {
        System.out.println("Loading profiles selector");
        currentProfile = null;
        garageFiles = null;

        // Remove previous items from the frame
        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();

        // Init a new basic panel
        JPanel panel = basicPanel();

        // Add a button for each profile in the array list
        profiles.forEach(profile -> {
            JButton profileButton = new JButton(profile.getName());
            panel.add(profileButton);

            profileButton.addActionListener(e -> {
                currentProfile = profile;
                loadProfile(true);
            });
        });

        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void loadProfile(boolean firstLoad) {
        if(firstLoad) {
            // If its the first load, set up the garage list
            System.out.println("Loading profile " + currentProfile.getName());
            garageFiles = new ArrayList<>();
            car1 = car2 = -1;
            b1 = b2 = null;

            for (File file : currentProfile.listFiles()) {
                if(file.getName().startsWith("#ParkingLvl1CarLoader") && file.getName().endsWith(".dat")) {
                    garageFiles.add(file);
                }
            }

            garageFiles.sort((file1, file2) -> {
                int id1 = Integer.parseInt(file1.getName().substring(21).split("\\.")[0]);
                int id2 = Integer.parseInt(file2.getName().substring(21).split("\\.")[0]);
                return id1 - id2;
            });
        }

        // Remove previous content
        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();

        JPanel panel = basicPanel();

        // Back button
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            loadProfileSelector();
        });
        panel.add(back);

        // Add a button for each garage floor
        for (int i = 1; i <= 14; i++) {
            final int level = i;

            JButton garageLevelButton = new JButton("Garage level " + i);
            garageLevelButton.addActionListener(e -> {
                loadGarageLevel(level);
            });

            panel.add(garageLevelButton);
        }

        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void loadGarageLevel (int level) {
        System.out.println("Loading garage level " + level);
        // Remove previous content
        frame.getContentPane().removeAll();
        frame.revalidate();
        frame.repaint();

        // Init basic panel
        JPanel panel = basicPanel();

        // Back button
        JButton back = new JButton("Back");
        back.addActionListener(e -> loadProfile(false));
        panel.add(back);

        // Loop through the range for this garage floor
        for (int i = ranges[level-1][0]; i <= ranges[level-1][1]; i++) {
            // Add a button for each car
            final int carId = i;
            JToggleButton button = new JToggleButton("Car " + (carId + 1));
            button.addActionListener(e -> {
                // If the first car ID isnt set, set it
                if (car1 == -1) {
                    car1 = carId;
                    b1 = button;
                    // If it is set, set the second car ID and swap their spot
                } else {
                    car2 = carId;
                    b2 = button;
                    swap();
                }
            });
            panel.add(button);
        }

        frame.add(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void swap() {
        File car1File = garageFiles.get(car1);
        File car2File = garageFiles.get(car2);

        File parentFolder = car1File.getParentFile();

        String car1FileName = car1File.getName();
        String car2FileName = car2File.getName();

        File tmp = new File(parentFolder, "tmpcmsgarageoderfile");
        File oneTo = new File(parentFolder, car2FileName);
        File twoTo = new File(parentFolder, car1FileName);

        System.out.println(tmp.getAbsolutePath());
        System.out.println(oneTo.getAbsolutePath());
        System.out.println(twoTo.getAbsolutePath());

        System.out.println(car1File.renameTo(tmp));
        System.out.println(car2File.renameTo(twoTo));
        System.out.println(tmp.renameTo(oneTo));

        tmp.delete();

        b1.setSelected(false);
        b2.setSelected(false);

        car1 = car2 = -1;
        b1 = b2 = null;
    }

    // Initialize a basic JPanel with center aligned content and a limited width
    private JPanel basicPanel() {
        JPanel newPanel = new JPanel();
        newPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        newPanel.setMaximumSize(new Dimension(122, HEIGHT));
        return newPanel;
    }

    private Path getPath(String path) {
        return FileSystems.getDefault().getPath(path);
    }
}

