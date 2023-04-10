# javaABC AI Population
Bacteria Simulation with Genetic Algorithm

## How to start:

1) Create a new IntelliJ project from Version Control

![clone](https://user-images.githubusercontent.com/52008460/231006420-8dbcd7af-f2af-452d-bb7f-54a55e3f5fef.PNG)


2) Clone the project

![clone2](https://user-images.githubusercontent.com/52008460/231006525-4f61d3f2-ea20-4e13-a9c5-74638dcdfaa1.PNG)


3) Open ```Simulation.java``` file in the ```src``` directory

![jdk](https://user-images.githubusercontent.com/52008460/231006777-39615e12-292a-47bc-8677-83bbc2261314.PNG)


4) Add JDK

![jdk2](https://user-images.githubusercontent.com/52008460/231006808-75b4397d-0bd8-42c9-b18c-ded3d8082a4d.PNG)


5) Mark ```resources``` folder as resource

![res](https://user-images.githubusercontent.com/52008460/231006901-fc8bb37c-1780-4900-b490-717d54029237.PNG)


6) Launch! You can close the simulation using ```ALT+F4```

![start](https://user-images.githubusercontent.com/52008460/231006920-8624f838-99ce-4ff7-8be6-018986905060.PNG)


## Save / Load

Every 5min (simulation time), the world is automatically saved in your home directory, e.g.
```
C:\Users\TimoF\.aipopulation\world.txt
```

If you close the window using ```ALT+F4```, the world will be saved too.

If you want to reset the world, just delete ```world.txt```. A new world will be created automatically.


## Controls

- ```SPACE```: pause / resume

- ```F```: toggle fast forward

- ```B```: toggle show fittest individual

- ```G```: toggle render generation for each individual

- ```O```: toggle show oldest individual

- ```M```: toggle show generation title and individual with highest generation
