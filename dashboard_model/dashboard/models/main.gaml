/**
* Name: model
* Based on the internal skeleton template. 
* Author: USER
* Tags: 
*/

model main

global {
	int nb_people <- 100;
	int nb_infected_init <- 10; 
	float step <- 5 #mn;
	geometry shape <- square(1500 #m);
	
	
	int dist_infected <- 5;
	float prob_infectious <- 0.9;
	
	
	
	init {
		create people number: nb_people;
		ask nb_infected_init among people {
			is_infected <- true;
		}
		
		create people2 number: 40;
	}
	
	// tham so theo doi
	int total_people <- nb_people update: people count(true);
	int total_people_isInfected <- nb_infected_init update: people count(each.is_infected);
	int total_people_notInfected <- nb_people - nb_infected_init update: people count(!each.is_infected);
//	int total_people_recovered <- 3;
}


species people skills:[moving]{
	float speed <- (2 + rnd(3)) #km/#h;
	bool is_infected <- false;
	bool is_tested <- false;
	bool is_quaranted <- false;
	
	int case_test <- 0;
	int case_infected <- 0;
	int case_recover <- 0;
	
	
	
	reflex move {
		do wander;
	}
	
	reflex infect when: is_infected  {
		ask people at_distance dist_infected # m {
			if (flip (prob_infectious) ){
				is_infected <- true;
			}
		}
	}
	
	reflex test when: flip(0.01) {
		is_tested <- true;
		case_test <- case_test + 1;
		if(is_infected){
			case_infected <- case_infected + 1;
			do recover;
		}
	}
	
	action recover {
		if(flip(0.08)){
			is_infected <- false;
			is_tested <- false;
			case_recover <- case_recover + 1;
		}
	}
	
	aspect base {
		draw circle(10) color: is_infected ? #red : #green;
	}
	
}


species people2 skills:[moving]{
	
	
	reflex move {
		do wander;
	}
	
	aspect base {
		draw square(20) color: #black;
	}
}


experiment main type: gui {
	parameter "Number people infected at itnit:" var: nb_infected_init min: 2 max: 100;
	
	output {
		
		display "main" view: "dashboard"{
			
			species people aspect:base;
			
			species people2 aspect:base;
			
			chart "People" type: pie {
				data "People is infected" value: total_people_isInfected color: #red;
				data "People is not infected" value: total_people_notInfected color: #blue;
			}
			
			chart "Test and Isolation" type: histogram {
				data "Case testing" value: people sum_of(each.case_test);
				data "Case infected" value: people sum_of(each.case_infected);
				data "Case recover" value: people sum_of(each.case_recover);
			}
			
			
		
			
//			graphics "new Point " {
//				
//					//Draw the function as a line
//					draw circle(10.0);
//					draw square(10.0);
//				
//				
//			}
			
			
		}
	}
}
