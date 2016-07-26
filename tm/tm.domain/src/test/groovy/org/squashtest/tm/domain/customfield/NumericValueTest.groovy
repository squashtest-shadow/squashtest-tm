package org.squashtest.tm.domain.customfield

import org.squashtest.tm.exception.customfield.WrongCufNumericFormatException
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Created by jthebault on 26/07/2016.
 */
class NumericValueTest extends Specification{

	@Unroll
	def "should reject invalid numbers" (){
		given:
		NumericValue numericValue = new NumericValue();

		when:
		numericValue.setValue(value);

		then:
		thrown(WrongCufNumericFormatException)


		where:
		value 			| _
		"toto"			| _
		"1,65,89"		| _
		"1 236 897"		| _
		"89+96"			| _
	}

	@Unroll
	def "should accept valid numbers" (){
		given:
		NumericValue numericValue = new NumericValue();

		when:
		numericValue.setValue(value);

		then:
		noExceptionThrown()


		where:
		value 			| _
		"1"				| _
		"1,6589"		| _
		"8971.6589"		| _
		"1236897"		| _
		"-8996"			| _
		"0"				| _
		"-0"			| _
	}

	@Unroll
	def "should convert decimal separator" (){
		given:
		NumericValue numericValue = new NumericValue();

		when:
		numericValue.setValue(value);

		then:
		numericValue.getValue().equals(expectedValue)


		where:
		value 			| expectedValue
		"1,00"			| "1.00"
		"1,6589"		| "1.6589"
		"8971.6589"		| "8971.6589"
		"1236897"		| "1236897"
		"-8996"			| "-8996"
		"0"				| "0"
		"-0"			| "0"
	}
}
