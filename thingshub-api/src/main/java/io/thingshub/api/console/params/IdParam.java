package io.thingshub.api.console.params;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdParam {

	@NotNull(message = "ID不能为空")
	private Long id;

}