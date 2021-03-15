package projects.gecco

import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueInteger
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.SimpleQuantity

import javax.persistence.criteria.Root

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "BODYMEASUREMENT_CODE") {
    return // no export
  }

  id = "BodyWeight/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-weight"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category{
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "vital-signs"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "29463-7"
    }
    coding{
      system = "http://snomed.info/sct"
      code = "27113001"
    }
  }

  subject{
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }


  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }


  final def bodyWeightLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
     "BODYWEIGHT_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (bodyWeightLfLv){
    valueQuantity {
      value = bodyWeightLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = "kg"
      system = "http://unitsofmeasure.org"
      code = "kg"
    }
  }

  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def bodyWeightAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (bodyWeightAnnotation){
    note{
      text = bodyWeightAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }

  if(bodyWeightLfLv){
    referenceRange {
      low {
        value = bodyWeightLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.LOWER_VALUE]
      }
      high {
        value = bodyWeightLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.UPPER_VALUE]
      }
    }
  }

}


