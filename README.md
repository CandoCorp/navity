# ![alt-text](https://navity.net/assets/navity.png)

Navity is a Wireless Sensor Network that allows having relevant information at sea, through our LPWAN network we can deploy smart sensors to help taking care of marine ecosystems, such as detecting the presence of unauthorized vessels, water pollution or help in early detection of floods in rivers.

# Motivation for Navity
Galapagos has always experienced problems with illegal fishing. In 2017 a chinese float captured almost 600000 pounds of shark fins, in the borders of Galapagos islands. Only in US the financial losses are value up to $23 billion per year. Cost estimates indicate that flooding has caused more than $480 million US in damage to homes, while businesses have suffered $300 million US in damage. Without considering the environmental loss that it does. 

# Navity description
We implemented a smart buoy which acts as a gateway for LPWAN communications. This means that each buoy has the capability of receiving LoRa and Sigfox messages and then if it is not possible, it would save them and retransmit. Near to the shore we will have nodes with connectivity to Global Sigfox Network and can send the data that the network have retained or it could send the packages over LoRa if there’s LoRa gateways deployed near the shore. Each of this buoys can be equipped with multiple sensors that can detect small or large boats in Oceans, measure water quality parameters or measure water flow and pressure in Rivers. 
Each buoy is going to be equipped with a LPWAN antenna, solar panels and a ocean wave power electric generator for self-sufficiency. For Galapagos application we will use passive sonar systems for detecting and classify transit vehicles in the area. Each buoy is going to have a radius of 50km, but we estimate placing each buoy at 30km radius from each other. This allows a big range of opportunities because other embarcations can use LPWAN technology devices to communicate over the provided network. 
Other application we are going to supply is in rivers, the buoy is going to be equipped with a pressure transducer and accelerometer to trace water flow and current. The movement of the buoy itself is going to be recorded. Each buoy will act as an link and form an array of buoys allowing us to early predict if there’s going to be an overflood. 
All of this information, is gathered and travels to the Navity platform, which allows users to set their own Rules from the sensor data, consume weather APIs and other climate data APIs, then they can use Watson Services to process and build ML models over it. The architecture itself is mounted using Kubernetes in IBM Cloud, Cloudant database.

# Why Navity?
Navity is design from the ground up to adapt with other Call for Code initiatives like Lali Wildfire, Project OWL or Project Lantern, each of them required a sustainable communication network as we forecast each of them have a partial solution over the land, but they are not extendable to the sea. We believe that our approach is design ready to be fast deploy since we don’t have to reinvent the technology and we can use components that already have been proved to work. We design our software platform as an extendable solution and we used as template an open source initiative, the idea for this is that each tenant that buys the solution can control its instances and use full power of IBM services.

## How it works

![alt text](https://media.giphy.com/media/XEs721CMUH6dn64kWz/giphy.gif)

![alt text](https://media.giphy.com/media/THlz70OO8Sv1IE35hx/giphy.gif)

![alt text](https://media.giphy.com/media/gkL1MughDCSm4afB5k/giphy.gif)

## Road Map

![alt text](https://navity.net/assets/road_map_navity.png)

## License [apache-2.0](https://choosealicense.com/licenses/apache-2.0/)

Client platform is based on [Thingsboard](https://thingsboard.io) implementation.
