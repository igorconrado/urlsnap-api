package com.urlsnap.url;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShortCodeGeneratorTest {

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    void generatedCodeIsSixCharacterAlphanumeric() {
        assertThat(generator.generateCode()).matches("[A-Za-z0-9]{6}");
    }

    @Test
    void collisionRetriesAreBounded() {
        UrlRepository repository = mock(UrlRepository.class);
        when(repository.existsByShortCode(anyString())).thenReturn(true);

        assertThatThrownBy(() -> generator.generate(repository))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("5 attempts");
    }
}
