/**
* Name: model
* Based on the internal skeleton template. 
* Author: USER
* Tags: 
*/

model main

global {
	/** Insert the global definitions, variables and actions here */
}

experiment main type: gui {
	/** Insert here the definition of the input and output of the model */
	output {
		
		/**
		 * dashboard_type Operator to set up sample layout with multi-display
		 */
//		layout dashboard_type("operational");
		
		
		dashboard "test" {
			/**
			 * Dashboard statement to show tool for creating dashboard in one display
			 */
//			dashboard "dash" type:"strategic" {}
			chart "sdasd" type:bubble {}
		}
		
				
	}
}
