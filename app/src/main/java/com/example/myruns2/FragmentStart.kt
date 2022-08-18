package com.example.myruns2



import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class FragmentStart : Fragment(){
    private lateinit var spinner:Spinner
    private lateinit var spinner2:Spinner
    private lateinit var root:View
    private lateinit var  button:Button
    private lateinit var  textActivityType:String
    private lateinit var  textInputType:String
    private  var  textActivityInt:Int = 0

    private val CHOICES  = arrayOf("one", "two", "three")
    override fun onCreateView(
        inflater:LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        root = inflater.inflate(R.layout.fragment_start, container, false)
        spinner = root.findViewById(R.id.coursesspinner)
        spinner2 = root.findViewById(R.id.coursesspinner2)
        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.entriesNUMNUMS,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter;


        }

        ArrayAdapter.createFromResource(
            requireActivity(),
            R.array.entriesNUMNUMS2,
            android.R.layout.simple_spinner_item
        ).also{ adapter->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter;


        }

       button = root.findViewById<Button>(R.id.button)

        button.setOnClickListener{
            println("debug: buttonClicked fucking kidding me?")

            textActivityType =  spinner2.selectedItem.toString()
            textInputType =  spinner.selectedItem.toString()
            if(textInputType=="Manual Entry"){
                val bundleToPass = Bundle()
                println("debug: textActivity type is like this"+ textActivityType+ "in FragmentStart")
                if(textActivityType == "Running"){
                   textActivityInt = 0
                }
                else if(textActivityType == "Walking"){
                    textActivityInt = 1
                }
                else if(textActivityType == "Standing"){
                    textActivityInt = 2

                }
                else if(textActivityType == "Cycling"){
                    textActivityInt = 3

                }
                else if(textActivityType == "Hiking"){
                    textActivityInt = 4

                }
                else if(textActivityType == "Downhill Skiing"){
                    textActivityInt = 5

                }
                else if(textActivityType == "Cross-Country Skiing"){
                    textActivityInt = 6

                }
                else if(textActivityType == "Snowboading"){
                    textActivityInt = 7

                }
                else if(textActivityType == "Skating"){
                    textActivityInt = 8

                }
                else if(textActivityType == "Swimming"){
                    textActivityInt = 9

                }
                else if(textActivityType == "Mountain Biking"){
                    textActivityInt = 10

                }
                else if(textActivityType == "Wheelchair"){
                    textActivityInt = 11

                }
                else if(textActivityType == "Eliptical"){

                    textActivityInt = 12
                }
                else if(textActivityType == "Other"){
                    textActivityInt = 13

                }
                println("debug: since activity is "+textActivityType+ ", i will pass this " + textActivityInt)
                bundleToPass.putInt("activityType", textActivityInt)
                bundleToPass.putInt("inputType", 0)
                //manual entry is 0

                //pass this text with sp.
                println("debug: input type is like this => " + textInputType)
                val intent = Intent(requireActivity(), StartButtonActivity::class.java)
                intent.putExtras(bundleToPass)
                startActivity(intent)
                //fucking ya. Good job Hayato. very impressive. You can do it.
            }
            else if(textInputType=="GPS"){
               println("debug: I will take you to the GPS page")

                val intent = Intent(requireActivity(), gpsActivity::class.java)
                startActivity(intent)
            }
            else if(textInputType=="Automatic"){
                println("debug: I will take you to the Automatic page")
                val intent = Intent(requireActivity(), automaticActivity::class.java)
                startActivity(intent)
            }
        }

        return root
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//      }
        spinner.setOnItemClickListener(){ parent: AdapterView<*>, textView: View, position:Int, id:Long->
            println("hey you just clicked")
        } 
    }


//   fun goToAnotherActivity(view:View?) {
//      println("debug: button Clicked")
//   }

}