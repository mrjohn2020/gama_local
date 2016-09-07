/**
* Name: Comodel of Boids and the Procedural City
* Author: HUYNH Quang Nghi
* Description: Co-model example : The Boids are moving in the Procedural City.
* Tags: comodel
 */
model city_boids
import "Adapters/Boids Adapter.gaml" as Boids
import "Adapters/Procedural City Adapter.gaml" as City
 

global
{
	int width_and_height_of_environment<-1000;
	// set the bound of the environment
	geometry shape <- envelope(width_and_height_of_environment);
	
	init
	{	
		//create experiment from micro-model Boids
		create Boids."Adapter" with: [
			shape::square(width_and_height_of_environment), 
			width_and_height_of_environment::width_and_height_of_environment, 
			z_max::100,
			number_of_agents::500
		];
		//create experiment form micro-model Procedural City
		create City."Adapter" 
		with:[
			 number_of_building::Boids."Adapter"[0].simulation.number_of_agents,
			width_and_height_of_environment::width_and_height_of_environment
		];
	}

	reflex simulate_micro_models
	{
 
		
		//loop over the population
		loop theBoid over: (Boids."Adapter"[0]).get_boids()
		{
			Building theBuilding <- Building((City."Adapter"[0]).get_building_at(theBoid)); 
			if(theBuilding != nil){				
				write theBoid distance_to theBuilding;
				if (theBoid distance_to theBuilding < theBuilding.width)
				{
					ask theBoid
					{
						location<- location translated_by ((theBoid.location-theBuilding.location));
					}
				}
			}
		}
		
		
		//tell myBoids to step a cycle
		ask (Boids."Adapter" collect each.simulation){ do _step_;}
	}

}
 
experiment main type: gui
{
	output
	{
		display "Comodel Display"  
		type:opengl
		{
			agents "Building" value: (City."Adapter" accumulate each.get_building()) aspect:textured;		
			
			agents "boids_goal" value: (Boids."Adapter" accumulate each.get_boids_goal()) ;
			
			agents "boids" value: (Boids."Adapter" accumulate each.get_boids())  aspect: image;
			
		}

	}

}
